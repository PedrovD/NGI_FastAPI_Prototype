package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

import java.io.IOException;

public class FileException extends GlobalException {
    public FileException(String message, IOException innerException) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, innerException);
    }
}
