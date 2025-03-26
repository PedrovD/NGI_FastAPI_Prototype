package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;

public class DatabaseInsertException extends GlobalException {
  public DatabaseInsertException(String message) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
