package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidDataException extends GlobalException {
    public InvalidDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
