package ru.itmo.wp.web.page;

import ru.itmo.wp.model.domain.EventType;
import ru.itmo.wp.model.domain.User;
import ru.itmo.wp.model.service.EventService;
import ru.itmo.wp.web.exception.RedirectException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@SuppressWarnings({"unused"})
public class LogoutPage extends Page {
    private final EventService eventService = new EventService();

    @Override
    public void action(HttpServletRequest request, Map<String, Object> view) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            eventService.save(user.getId(), EventType.LOGOUT);
        }
        request.getSession().removeAttribute("user");
        setMessage("Good bye. Hope to see you soon!");
        throw new RedirectException("/index");
    }
}
