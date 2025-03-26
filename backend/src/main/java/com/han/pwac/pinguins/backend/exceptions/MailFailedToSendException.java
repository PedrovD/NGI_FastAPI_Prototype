package com.han.pwac.pinguins.backend.exceptions;

import com.han.pwac.pinguins.backend.domain.Mail;
import org.springframework.http.HttpStatus;

public class MailFailedToSendException extends GlobalException {
    private final Mail mail;

    public MailFailedToSendException(String message, Mail mail) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        this.mail = mail;
    }

    public Mail getMail() {
        return mail;
    }
}
