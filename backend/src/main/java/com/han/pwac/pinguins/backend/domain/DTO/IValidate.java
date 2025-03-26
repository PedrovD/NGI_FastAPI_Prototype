package com.han.pwac.pinguins.backend.domain.DTO;

public interface IValidate {
    boolean isValid();


    public static String stripMarkdownFromString(String string) {
        String replacement = string
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("__(.+?)__", "$1")
                .replaceAll("_(.+?)_", "$1")
                .replaceAll("~~(.+?)~~", "$1")
                .replaceAll("`(.+?)`", "$1")
                .replaceAll("&gt; (.+?)", "$1")
                .replaceAll("\\* {3}(.+?)", "$1")
                .replaceAll("\\d+. +(.+?)", "$1")
                .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1")
                .replaceAll("(?m)^## (.+?)", "$1")
                .replaceAll("(?m)^# (.+?)", "$1")
                .replaceAll("[\\r\\n]+", "\n")
                .replaceAll("\\n+ *?\\n+", "\n")
                .trim();
        return replacement;
    }
}
