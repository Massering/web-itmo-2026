package ru.itmo.wp.servlet;

import ru.itmo.wp.util.ImageUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CaptchaFilter implements Filter {
    private static final String CAPTCHA_URL = "/captcha";
    private static final String SESSION_EXPECTED = "captchaExpected";
    private static final String SESSION_PASSED = "captchaPassed";

    private final Random random = new Random();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession();
        String uri = request.getRequestURI();
        String path = uri.substring(request.getContextPath().length());

        // капча пройдена
        if (Boolean.TRUE.equals(session.getAttribute(SESSION_PASSED))) {
            chain.doFilter(request, response);
            return;
        }

        // запрос картинки капчи
        if (CAPTCHA_URL.equals(path)) {
            Object expected = session.getAttribute(SESSION_EXPECTED);
            if (expected == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-store");
            try (OutputStream out = response.getOutputStream()) {
                out.write(ImageUtils.toPng(expected.toString()));
            }
            return;
        }

        // ответ на капчу
        String answer = request.getParameter("captcha");
        if ("POST".equalsIgnoreCase(request.getMethod()) && answer != null) {
            Object expected = session.getAttribute(SESSION_EXPECTED);

            if (expected != null && expected.toString().equals(answer.trim())) {
                session.setAttribute(SESSION_PASSED, true);
                session.removeAttribute(SESSION_EXPECTED);
                response.sendRedirect(response.encodeRedirectURL(request.getRequestURI()));
                return;
            }

            session.removeAttribute(SESSION_EXPECTED);
            showCaptcha(request, response, session);
            return;
        }

        // показываем капчу
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            showCaptcha(request, response, session);
            return;
        }

        chain.doFilter(request, response);
    }

    private void showCaptcha(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        // загадываем новое число, если его ещё нет
        if (session.getAttribute(SESSION_EXPECTED) == null) {
            int code = 100 + random.nextInt(900);
            session.setAttribute(SESSION_EXPECTED, code);
        }

        String contextPath = request.getContextPath();
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().print(
                "<!DOCTYPE html>" +
                        "<html><head><meta charset=\"UTF-8\"><title>Captcha</title></head>" +
                        "<body style=\"text-align:center;font-family:sans-serif;margin-top:80px\">" +
                        "<h2>Введите код с картинки</h2>" +
                        "<img src=\"" + contextPath + CAPTCHA_URL + "?t=" + System.currentTimeMillis() + "\" alt=\"captcha\">" +
                        "<br><br>" +
                        "<form method=\"POST\">" +
                        "<input type=\"text\" name=\"captcha\" autocomplete=\"off\" autofocus required>" +
                        " " +
                        "<button type=\"submit\">Проверить</button>" +
                        "</form>" +
                        "</body></html>"
        );
    }
}
