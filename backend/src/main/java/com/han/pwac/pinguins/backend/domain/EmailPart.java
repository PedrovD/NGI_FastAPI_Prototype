package com.han.pwac.pinguins.backend.domain;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.stream.Stream;

public class EmailPart implements CharSequence {
    private final StringBuilder builder;
    private int currentIndex;
    private final int startIndex;
    private final String partName;

    public static final String BORDER = "<div style=\"border-bottom: 1px solid black; margin: 0.5rem 0;\"></div>";

    public EmailPart(StringBuilder builder, int startIndex, String partName) {
        this.builder = builder;
        this.currentIndex = startIndex;
        this.partName = partName;
        this.startIndex = startIndex;
    }

    public EmailPart(String partName) {
        this.builder = new StringBuilder(256);
        this.currentIndex = 0;
        this.startIndex = 0;
        this.partName = partName;
    }

    private void insertText(String text) {
        builder.insert(currentIndex, text);
        currentIndex += text.length();
    }

    public void addMessage(String text) {
        insertText("<p>" + text + "</p>");
    }

    public void addLinkButton(String text, String link) {
        addLinkButton(text, link, false);
    }

    public void addLinkButton(String text, String link, boolean wFull) {
        String button = """
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary" style="width: 100%;">
                    <tbody>
                        <tr>
                            <td align="left">
                                <table role="presentation" border="0" cellpadding="0" cellspacing="0" TABLE_STYLE>
                                    <tbody>
                                    <tr>
                                        <td> <a href="TEMPLATE_LINK" target="_blank">TEMPLATE_TEXT</a> </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    </tbody>
                </table>
                """;

        String style = wFull ? "style=\"width: 100%;\"" : "";

        String buttonWithTextAndLink = button
                .replace("TEMPLATE_LINK", link)
                .replace("TEMPLATE_TEXT", text)
                .replace("TABLE_STYLE", style);

        insertText(buttonWithTextAndLink);
    }

    public void addList(Iterator<String> listItems) {
        insertText("<ul>");

        while (listItems.hasNext()) {
            String item = listItems.next();

            insertText("<li>" + item + "</li>");
        }

        insertText("</ul>");
    }

    public enum HeadingSize {
        H1("<h1>"),
        H2("<h2>"),
        H3("<h3>"),
        H4("<h4>"),
        H5("<h5>"),
        H6("<h6>");

        private final String tag;

        HeadingSize(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        public String getClosingTag() {
            return tag.replace("<", "</");
        }
    }

    public void addHeading(String text, HeadingSize size) {
        insertText(size.getTag() + text + size.getClosingTag());
    }

    public void addBorderAtBottom() {
        insertText(BORDER);
    }

    public void addPlainHtml(String html) {
        insertText(html);
    }

    public String getPartName() {
        return partName;
    }

    @Override
    public int length() {
        return currentIndex - startIndex;
    }

    @Override
    public char charAt(int index) {
        return builder.charAt(index + startIndex);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (end >= length()) {
            throw new IllegalArgumentException("End out of bounds");
        }

        return builder.subSequence(start + startIndex, end + startIndex);
    }

    @Override
    public String toString() {
        return builder.subSequence(startIndex, currentIndex).toString();
    }
}
