package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class IncorrectCredentialsException extends GlobalException {
    public IncorrectCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
