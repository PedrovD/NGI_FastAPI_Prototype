package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends GlobalException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
