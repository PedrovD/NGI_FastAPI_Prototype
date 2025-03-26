package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class InternalFileUploadException extends GlobalException {
  public InternalFileUploadException(String message) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
