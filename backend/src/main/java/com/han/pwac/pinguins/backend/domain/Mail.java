package com.han.pwac.pinguins.backend.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class Mail {
    private final String subject;
    private final String to;
    private final EmailBody body;

    public Mail(
            String subject,
            String to,
            EmailBody body
    ) {
        this.subject = subject;
        this.to = to;
        this.body = body;
    }
    
    public String from() {
        return "opdrachtenbox@han.nl";
    }

    public String subject() {
        return subject;
    }

    public String to() {
        return to;
    }

    public EmailBody body() {
        return body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Mail) obj;
        return Objects.equals(this.subject, that.subject) &&
                Objects.equals(this.to, that.to) &&
                Objects.equals(this.body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, to, body);
    }

    @Override
    public String toString() {
        return "Mail[" +
                "subject=" + subject + ", " +
                "to=" + to + ", " +
                "body=" + body + ']';
    }

}
