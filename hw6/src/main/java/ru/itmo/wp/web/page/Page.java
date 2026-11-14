package ru.itmo.wp.web.page;

import com.google.common.base.Strings;
import ru.itmo.wp.model.domain.User;
import ru.itmo.wp.model.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class Page {
    private final UserService userService = new UserService();

    protected HttpServletRequest request;
    protected Map<String, Object> view;

    public void before(HttpServletRequest request, Map<String, Object> view) {
        this.request = request;
        this.view = view;

        User user = (User) request.getSession().getAttribute("user");
        view.put("user", user);
        view.put("userCount", userService.findCount());

        String message = (String) request.getSession().getAttribute("message");
        if (!Strings.isNullOrEmpty(message)) {
            view.put("message", message);
            request.getSession().removeAttribute("message");
        }
    }

    public void after(HttpServletRequest request, Map<String, Object> view) {
        // No operations.
    }

    public void action(HttpServletRequest request, Map<String, Object> view) {
        // No operations.
    }

    public void setMessage(String message) {
        request.getSession().setAttribute("message", message);
    }
}
