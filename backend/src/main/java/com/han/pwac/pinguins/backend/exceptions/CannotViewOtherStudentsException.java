package com.han.pwac.pinguins.backend.exceptions;

import java.io.IOException;

public class CannotViewOtherStudentsException extends RuntimeException {
    public CannotViewOtherStudentsException(String message) {
        super(message);
    }
}
