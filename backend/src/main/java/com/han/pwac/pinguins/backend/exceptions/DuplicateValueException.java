package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateValueException extends GlobalException {
    public DuplicateValueException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
