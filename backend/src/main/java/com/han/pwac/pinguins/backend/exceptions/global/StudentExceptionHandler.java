package com.han.pwac.pinguins.backend.exceptions.global;

import com.han.pwac.pinguins.backend.domain.ErrorResponse;
import com.han.pwac.pinguins.backend.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class StudentExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> cannotViewOtherStudentsExceptionHandler(CannotViewOtherStudentsException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> studentNotFoundExceptionHandler(StudentNotFoundException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }
}