package com.han.pwac.pinguins.backend.domain;

import com.han.pwac.pinguins.backend.exceptions.EmailPartNotFound;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class EmailBody implements CharSequence {
    private final StringBuilder builder;

    private static String emailTemplate;

    public EmailBody(String frontendUrl) {
        this(true, frontendUrl);
    }

    public EmailBody(boolean useTemplate, String frontendUrl) {
        if (useTemplate) {
            builder = new StringBuilder(getEmailTemplate());
            Optional<EmailPart> part = getPart("frontendUrl");
            part.ifPresent(emailPart -> emailPart.addPlainHtml(frontendUrl));
        } else {
            builder = new StringBuilder(4096);
        }
    }

    public EmailBody(Collection<EmailPart> parts, String frontendUrl) {
        this(frontendUrl);

        HashMap<String, Integer> partIndexes = new HashMap<>(4);
        for (EmailPart part : parts) {
            String partName = part.getPartName();
            int index = partIndexes.computeIfAbsent(partName, this::getPartIndex);
            if (index == -1) {
                throw new EmailPartNotFound(partName);
            }

            String partString = part.toString();
            builder.insert(index, partString);
            index += partString.length();

            index += insertBorder(index);

            partIndexes.put(partName, index);
        }


    }

    private String getEmailTemplate() {
        if (emailTemplate == null) {
            for (int i = 0; i < 3; i++) {
                try {
                    emailTemplate = Files.readString(Paths.get("src/main/resources/templates/email_template.html"));
                    break;
                } catch (IOException ignored) {
                    emailTemplate = "";
                }
            }
        }

        return emailTemplate;
    }

    private int insertBorder(int index) {
        String borderString = EmailPart.BORDER;
        builder.insert(index, borderString);

        return borderString.length();
    }

    @Override
    public int length() {
        return builder.length();
    }

    @Override
    public char charAt(int index) {
        return builder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }

    private int getPartIndex(String name) {
        int currentIndex = 0;
        int partIndex;
        while ((partIndex = builder.indexOf("@part", currentIndex)) != -1) {
            currentIndex = partIndex + "@part".length();

            int endIndex = builder.indexOf(")", partIndex);
            if (endIndex == -1) {
                continue;
            }

            int quoteEndIndex = builder.lastIndexOf("\"", endIndex);
            if (quoteEndIndex == -1) {
                continue;
            }

            int quoteStartIndex = builder.lastIndexOf("\"", quoteEndIndex - 1);
            if (quoteStartIndex == -1) {
                continue;
            }

            String partName = builder.substring(quoteStartIndex + 1, quoteEndIndex);
            if (partName.equals(name)) {
                return endIndex + 1;
            }
        }

        return -1;
    }

    public Optional<EmailPart> getPart(String name) {
        int partIndex = getPartIndex(name);
        if (partIndex != -1) {
            return Optional.of(new EmailPart(builder, partIndex, name));
        }
        return Optional.empty();
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    @Override
    public String toString() {
        return builder.toString().replaceAll("@part\\(\".+?\"\\)", "");
    }

    public boolean contains(String value) {
        return builder.indexOf(value) != -1;
    }
}
