package ru.itmo.wp.web;

import freemarker.template.*;
import ru.itmo.wp.web.exception.NotFoundException;
import ru.itmo.wp.web.exception.RedirectException;
import ru.itmo.wp.web.page.IndexPage;
import ru.itmo.wp.web.page.NotFoundPage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrontServlet extends HttpServlet {
    private static final String BASE_PACKAGE = FrontServlet.class.getPackage().getName() + ".page";
    private static final String DEFAULT_ACTION = "action";

    private Configuration sourceConfiguration;
    private Configuration targetConfiguration;

    private Configuration newFreemarkerConfiguration(String templateDirName, boolean debug)
            throws ServletException {
        File templateDir = new File(templateDirName);
        if (!templateDir.isDirectory()) {
            return null;
        }

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        try {
            configuration.setDirectoryForTemplateLoading(templateDir);
        } catch (IOException e) {
            throw new ServletException("Can't create freemarker configuration [templateDir="
                    + templateDir + "]");
        }
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());

        // ВАЖНО: отключаем автоматический выбор локализованных шаблонов
        configuration.setLocalizedLookup(false);

        configuration.setTemplateExceptionHandler(debug ? TemplateExceptionHandler.HTML_DEBUG_HANDLER :
                TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setCacheStorage(new freemarker.cache.NullCacheStorage());
        configuration.setTemplateUpdateDelayMilliseconds(0);

        return configuration;
    }

    @Override
    public void init() throws ServletException {
        sourceConfiguration = newFreemarkerConfiguration(
                getServletContext().getRealPath("/") + "../../src/main/webapp/WEB-INF/templates", true);
        targetConfiguration = newFreemarkerConfiguration(
                getServletContext().getRealPath("WEB-INF/templates"), false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        updateLanguage(request);    // task 2
        Route route = Route.newRoute(request);
        try {
            process(route, request, response);
        } catch (NotFoundException e) {
            try {
                process(Route.newNotFoundRoute(), request, response);
            } catch (NotFoundException notFoundException) {
                throw new ServletException(notFoundException);
            }
        }
    }

    private void process(Route route, HttpServletRequest request, HttpServletResponse response)
            throws NotFoundException, ServletException, IOException {
        Class<?> pageClass;
        try {
            pageClass = Class.forName(route.getClassName());
        } catch (ClassNotFoundException e) {
            throw new NotFoundException();
        }

        // Ищем action-метод по имени, без требования к сигнатуре,
        // с обходом предков и интерфейсов (включая default-методы).
        Method method = findActionMethod(pageClass, route.getAction());
        if (method == null) {
            throw new NotFoundException();
        }

        Object page;
        try {
            page = pageClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new ServletException("Can't create page [pageClass="
                    + pageClass + "]");
        }

        Map<String, Object> view = new HashMap<>();
        method.setAccessible(true);
        Object[] args = buildArguments(method, request, view);
        try {
            method.invoke(page, args);
        } catch (IllegalAccessException e) {
            throw new ServletException("Can't invoke action method [pageClass="
                    + pageClass + ", method=" + method + "]");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RedirectException) {
                RedirectException redirectException = (RedirectException) cause;
                response.sendRedirect(redirectException.getTarget());
                return;
            } else {
                throw new ServletException("Can't invoke action method [pageClass="
                        + pageClass + ", method=" + method + "]", cause);
            }
        }

        String lang = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            lang = (String) session.getAttribute(SESSION_LANG);
        }

        System.out.println(">>> pageClass=" + pageClass.getSimpleName()
                + " lang=" + lang
                + " sessionLang=" + (request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("lang")));

        Template template = newTemplate(pageClass.getSimpleName(), lang);

        response.setContentType("text/html");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            template.process(view, response.getWriter());
        } catch (TemplateException e) {
            if (sourceConfiguration == null) {
                throw new ServletException("Can't render template [pageClass="
                        + pageClass + ", action=" + method + "]", e);
            }
        }
    }

    /**
     * Ищет action-метод по имени.
     * Обходит класс, всех его предков и все реализованные интерфейсы.
     */
    private Method findActionMethod(Class<?> pageClass, String actionName) {
        for (Class<?> clazz = pageClass; clazz != null; clazz = clazz.getSuperclass()) {
            Method method = findInClass(clazz, actionName);
            if (method != null) {
                return method;
            }
            for (Class<?> iface : clazz.getInterfaces()) {
                method = findInInterface(iface, actionName);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    private Method findInClass(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    private Method findInInterface(Class<?> iface, String name) {
        for (Method method : iface.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        for (Class<?> parent : iface.getInterfaces()) {
            Method method = findInInterface(parent, name);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    /**
     * Строит массив аргументов в порядке, который требует сам метод.
     * Map - view, HttpServletRequest - request
     */
    private Object[] buildArguments(Method method, HttpServletRequest request, Map<String, Object> view) {
        Class<?>[] types = method.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            if (types[i] == HttpServletRequest.class) {
                args[i] = request;
            } else if (types[i] == Map.class) {
                args[i] = view;
            }
        }
        return args;
    }

    /**
     * Загружает шаблон с учётом языка из сессии.
     * Если lang != null и lang != "en", сначала пробует <baseName>_<lang>.ftlh,
     * иначе/если не нашёл — <baseName>.ftlh.
     */
    private Template newTemplate(String baseName, String lang) throws ServletException {
        if (lang != null && !"en".equals(lang)) {
            Template localized = tryLoadTemplate(baseName + "_" + lang + ".ftlh");
            if (localized != null) {
                System.out.println(">>> LOADED: " + localized.getSourceName());
                return localized;
            }
        }
        Template template = tryLoadTemplate(baseName + ".ftlh");
        if (template == null) {
            throw new ServletException("Can't find template [templateName=" + baseName + ".ftlh" + "]");
        }
        System.out.println(">>> LOADED: " + template.getSourceName());
        return template;
    }

    /**
     * Пытается загрузить шаблон: сначала из src, потом из target.
     * Если шаблона нет ни там, ни там — возвращает null.
     */
    private Template tryLoadTemplate(String templateName) throws ServletException {
        if (sourceConfiguration != null) {
            try {
                return sourceConfiguration.getTemplate(templateName);
            } catch (TemplateNotFoundException ignored) {
                // fall through to targetConfiguration
            } catch (IOException e) {
                throw new ServletException("Can't load template " + templateName, e);
            }
        }
        if (targetConfiguration != null) {
            try {
                return targetConfiguration.getTemplate(templateName);
            } catch (TemplateNotFoundException ignored) {
                // fall through to null
            } catch (IOException e) {
                throw new ServletException("Can't load template " + templateName, e);
            }
        }
        return null;
    }

    private static class Route {
        private final String className;
        private final String action;

        private Route(String className, String action) {
            this.className = className;
            this.action = action;
        }

        private String getClassName() {
            return className;
        }

        private String getAction() {
            return action;
        }

        private static Route newNotFoundRoute() {
            return new Route(NotFoundPage.class.getName(), DEFAULT_ACTION);
        }

        private static Route newIndexRoute() {
            return new Route(IndexPage.class.getName(), DEFAULT_ACTION);
        }

        private static Route newRoute(HttpServletRequest request) {
            String uri = request.getRequestURI();

            List<String> classNameParts = Arrays.stream(uri.split("/"))
                    .filter(part -> !part.isEmpty())
                    .collect(Collectors.toList());

            if (classNameParts.isEmpty()) {
                return newIndexRoute();
            }

            StringBuilder simpleClassName = new StringBuilder(classNameParts.get(classNameParts.size() - 1));
            int lastDotIndex = simpleClassName.lastIndexOf(".");
            simpleClassName.setCharAt(lastDotIndex + 1,
                    Character.toUpperCase(simpleClassName.charAt(lastDotIndex + 1)));
            classNameParts.set(classNameParts.size() - 1, simpleClassName.toString());

            String className = BASE_PACKAGE + "." + String.join(".", classNameParts) + "Page";

            String action = request.getParameter("action");
            if (action == null || action.isEmpty()) {
                action = DEFAULT_ACTION;
            }

            return new Route(className, action);
        }
    }

    private static final String LANG_PARAM = "lang";
    private static final String SESSION_LANG = "lang";

    private void updateLanguage(HttpServletRequest request) {
        String lang = request.getParameter(LANG_PARAM);
        if (lang != null && lang.matches("[a-z]{2}")) {
            request.getSession().setAttribute(SESSION_LANG, lang);
        }
    }
}
