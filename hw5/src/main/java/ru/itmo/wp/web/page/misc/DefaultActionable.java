package ru.itmo.wp.web.page.misc;

import java.util.Map;

@SuppressWarnings({"unused", "RedundantSuppression"})
public interface DefaultActionable {
    default void action(Map<String, Object> view) {
        view.put("message", "hello default");
    }
}