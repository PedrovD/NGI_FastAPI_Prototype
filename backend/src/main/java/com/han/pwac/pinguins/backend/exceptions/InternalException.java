package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class InternalException extends GlobalException {
    public InternalException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
