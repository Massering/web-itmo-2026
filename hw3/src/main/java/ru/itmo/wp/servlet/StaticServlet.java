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
        // запоминаем путь
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

        // просто разбиваем по плюсам и циклом проходим
        String[] parts = uri.split("\\+");

        File[] files = new File[parts.length];
        Path[] srcFiles = new Path[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.startsWith("/")) {
                part = "/" + part;
            }
            srcFiles[i] = null;
            files[i] = null;

            if (srcStaticDir != null) {
                Path f = srcStaticDir.resolve("." + part).normalize();
                if (f.startsWith(srcStaticDir) && Files.isRegularFile(f)) {
                    srcFiles[i] = f;
                    continue;
                }
            }
            String real = getServletContext().getRealPath("/static" + part);
            if (real != null) {
                File f = new File(real);
                if (f.isFile()) {
                    files[i] = f;
                    continue;
                }
            }
            // ни в src, ни в target/hw3 не нашли - 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // MIME-тип делаем по первому файлу
        String firstName = (srcFiles[0] != null)
                ? srcFiles[0].getFileName().toString()
                : files[0].getName();
        String contentType = getServletContext().getMimeType(firstName);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Cache-Control", "no-store");

        try (OutputStream out = response.getOutputStream()) {
            for (int i = 0; i < parts.length; i++) {
                if (srcFiles[i] != null) {
                    Files.copy(srcFiles[i], out);
                } else {
                    Files.copy(files[i].toPath(), out);
                }
            }
        }
    }
}