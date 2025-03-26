package com.han.pwac.pinguins.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GlobalException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public GlobalException(String message, HttpStatus status) {
        this(message, status, (String)null);
    }

    public GlobalException(String message, HttpStatus status, Throwable innerException) {
        this(message, status, null, innerException);
    }

    public GlobalException(String message, HttpStatus status, String error) {
        this(message, status, error, null);
    }

    public GlobalException(String message, HttpStatus status, String error, Throwable innerException) {
        super(message, innerException);

        this.status = status;

        this.error = Objects.requireNonNullElseGet(error, this::createError);
    }

    private String createError() {
        String className = this.getClass().getSimpleName();
        StringBuilder builder = new StringBuilder(className);

        int exceptionIndex = builder.indexOf("Exception"); // remove exception from class name
        if (exceptionIndex != -1) {
            builder.replace(exceptionIndex, exceptionIndex + "Exception".length(), "");
        }

        for (int i = 1; i < builder.length(); i++) {
            char c = builder.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.insert(i, ' ');
                i++;
            }
        }

        return builder.toString();
    }

    public ResponseEntity<Map<String, Object>> createErrorResponse() {
        HashMap<String, Object> map = new HashMap<>(4);

        map.put("timestamp", LocalDateTime.now());
        map.put("status", status.value());
        map.put("error", error);
        map.put("message", getMessage());

        return ResponseEntity.status(status.value()).body(map);
    }
}
