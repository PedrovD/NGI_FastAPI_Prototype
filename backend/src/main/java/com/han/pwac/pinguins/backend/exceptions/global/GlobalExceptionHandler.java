package com.han.pwac.pinguins.backend.exceptions.global;

import com.han.pwac.pinguins.backend.exceptions.GlobalException;
import com.han.pwac.pinguins.backend.exceptions.IncorrectCredentialsException;
import com.han.pwac.pinguins.backend.exceptions.InvalidDataException;

import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.TaskInvalidBodyException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.AuthenticationException;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String VALIDATION_ERROR = "Validation Error";
    
    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> genericErrorResponse(GlobalException ex) {
        return ex.createErrorResponse();
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> validationException(HandlerMethodValidationException ex) {
        Collection<? extends MessageSourceResolvable> errors = ex.getAllErrors();
        String message = errors.stream()
                .map(GlobalExceptionHandler::mapError)
                .collect(Collectors.joining(" en "));

        return createErrorResponse(HttpStatus.BAD_REQUEST, message, Optional.of(VALIDATION_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> validationException2(MethodArgumentNotValidException ex) {
        var errors = ex.getAllErrors();
        String message = errors.stream()
                .map(GlobalExceptionHandler::mapError)
                .collect(Collectors.joining(" en "));


        return createErrorResponse(HttpStatus.BAD_REQUEST, message, Optional.of(VALIDATION_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> validationException3(MissingServletRequestParameterException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getParameterName() + " was vereist maar niet gevonden.", Optional.of(VALIDATION_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> validationException4(MethodArgumentTypeMismatchException ex) {
        MethodParameter parameter = ex.getParameter();
        Optional<String> translation = getClassTranslation(ex.getRequiredType());
        
        String paramName = parameter.getParameterName();
        
        String message;
        message = translation
                .map(s -> "voor " + paramName + " was een " + s + " waarde verwacht.")
                .orElseGet(() -> "voor " + paramName + " is een onverwachte waarde meegegeven.");


        return createErrorResponse(HttpStatus.BAD_REQUEST, message, Optional.of(VALIDATION_ERROR));
    }

    private Optional<String> getClassTranslation(Class<?> clazz) {
        if (isNumberTypeClass(clazz)) {
            return Optional.of("nummer");
        } else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            return Optional.of("ja/nee");
        } else if (clazz.equals(Character.class) || clazz.equals(Character.TYPE)) {
            return Optional.of("karakter");
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return Optional.of("tekst");
        } else if (Calendar.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)) {
            return Optional.of("datum");
        }
        return Optional.empty();
    }

    private boolean isNumberTypeClass(Class<?> clazz) {
        return clazz.equals(Integer.class) || clazz.equals(Integer.TYPE) ||
                clazz.equals(Long.class) || clazz.equals(Long.TYPE) ||
                clazz.equals(Byte.class) || clazz.equals(Byte.TYPE) ||
                clazz.equals(Short.class) || clazz.equals(Short.TYPE);
    }

    private static String mapError(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();

        Object[] args = error.getArguments();
        if (args != null && args.length != 0 && args[0] instanceof MessageSourceResolvable name) {
            return name.getDefaultMessage() + " " + message;
        }

        return message;
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Optional.of("Authenticatie mislukt"));
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, Optional<String> error) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        error.ifPresent(s -> body.put("error", s));
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
