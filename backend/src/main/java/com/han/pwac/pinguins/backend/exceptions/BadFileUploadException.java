package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadFileUploadException extends GlobalException {
  public BadFileUploadException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
