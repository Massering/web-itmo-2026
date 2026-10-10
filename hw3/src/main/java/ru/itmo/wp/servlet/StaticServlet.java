package ru.itmo.wp.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticServlet extends HttpServlet {
    private Path srcStaticDir;

    @Override
    public void init() {
        String real = getServletContext().getRealPath("/static");
        if (real == null) {
            return;
        }
        Path p = Paths.get(real).normalize();
        while (p != null && !Files.exists(p.resolve("pom.xml"))) {
            p = p.getParent();
        }
        if (p != null) {
            srcStaticDir = p.resolve("src/main/webapp/static").normalize();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();

        if (uri.contains("..")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (srcStaticDir != null) {
            Path srcFile = srcStaticDir.resolve("." + uri).normalize();
            if (srcFile.startsWith(srcStaticDir) && Files.isRegularFile(srcFile)) {
                response.setContentType(getServletContext().getMimeType(srcFile.getFileName().toString()));
                try (OutputStream outputStream = response.getOutputStream()) {
                    Files.copy(srcFile, outputStream);
                }
                return;
            }
        }

        File file = new File(getServletContext().getRealPath("/static" + uri));
        if (file.isFile()) {
            response.setContentType(getServletContext().getMimeType(file.getName()));
            try (OutputStream outputStream = response.getOutputStream()) {
                Files.copy(file.toPath(), outputStream);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}