package ru.itmo.wp.servlet;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageServlet extends HttpServlet {
    private final List<Message> messages = new ArrayList<>();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();

        if (path == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        if ("/auth".equals(path)) {
            String user = request.getParameter("user");
            if (user != null) {
                session.setAttribute("user", user);
            }
            Object current = session.getAttribute("user");
            writeJson(response, current == null ? "" : current.toString());
        } else if ("/findAll".equals(path)) {
            writeJson(response, messages);
        } else if ("/add".equals(path)) {
            Object current = session.getAttribute("user");
            if (current == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String text = request.getParameter("text");
            messages.add(new Message(current.toString(), text == null ? "" : text));
            writeJson(response, "");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.getWriter().print(gson.toJson(object));
        response.getWriter().flush();
    }

    public static class Message {
        private final String user;
        private final String text;

        public Message(String user, String text) {
            this.user = user;
            this.text = text;
        }
    }
}