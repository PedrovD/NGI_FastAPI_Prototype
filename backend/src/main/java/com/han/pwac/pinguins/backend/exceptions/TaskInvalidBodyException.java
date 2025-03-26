package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class TaskInvalidBodyException extends GlobalException {
    public TaskInvalidBodyException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
