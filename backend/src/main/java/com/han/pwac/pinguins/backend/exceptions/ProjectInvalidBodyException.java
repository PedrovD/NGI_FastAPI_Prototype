package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class ProjectInvalidBodyException extends GlobalException {
  public ProjectInvalidBodyException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
