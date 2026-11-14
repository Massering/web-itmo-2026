package ru.itmo.wp.web.page;

import ru.itmo.wp.model.domain.User;
import ru.itmo.wp.model.exception.ValidationException;
import ru.itmo.wp.model.service.TalkService;
import ru.itmo.wp.model.service.UserService;
import ru.itmo.wp.web.exception.RedirectException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@SuppressWarnings({"unused"})
public class TalksPage extends Page {
    private final UserService userService = new UserService();
    private final TalkService talkService = new TalkService();

    public void action(HttpServletRequest request, Map<String, Object> view) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new RedirectException("/index");
        }
        fillView(user, view);
    }

    private void send(HttpServletRequest request, Map<String, Object> view) throws ValidationException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new RedirectException("/index");
        }

        fillView(user, view);

        String targetUserIdRaw = request.getParameter("targetUserId");
        String text = request.getParameter("text");

        talkService.validateSend(user.getId(), targetUserIdRaw, text);
        talkService.send(user.getId(), Long.parseLong(targetUserIdRaw), text);

        setMessage("Message sent");
        throw new RedirectException("/talks");
    }

    private void fillView(User user, Map<String, Object> view) {
        view.put("users", userService.findAll());
        view.put("talks", talkService.findAllByUserId(user.getId()));
    }
}
