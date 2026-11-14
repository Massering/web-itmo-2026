package ru.itmo.wp.web.page;

import ru.itmo.wp.model.domain.EventType;
import ru.itmo.wp.model.domain.User;
import ru.itmo.wp.model.exception.ValidationException;
import ru.itmo.wp.model.service.EventService;
import ru.itmo.wp.model.service.UserService;
import ru.itmo.wp.web.annotation.Json;
import ru.itmo.wp.web.exception.RedirectException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@SuppressWarnings({"unused"})
public class EnterPage extends Page {
    private final UserService userService = new UserService();
    private final EventService eventService = new EventService();

    @Json
    public void enter(HttpServletRequest request, Map<String, Object> view) throws ValidationException {
        String loginOrEmail = request.getParameter("loginOrEmail");
        String password = request.getParameter("password");

        userService.validateEnter(loginOrEmail, password);
        User user = userService.findByLoginOrEmailAndPassword(loginOrEmail, password);

        eventService.save(user.getId(), EventType.ENTER);

        request.getSession().setAttribute("user", user);
        setMessage("Hello, " + user.getLogin());
        throw new RedirectException("/index");
    }
}
