package com.han.pwac.pinguins.backend.exceptions;

public class EmailPartNotFound extends NotFoundException {
    public EmailPartNotFound(String part) {
        super("Email part: '" + part + "', kan niet gevonden worden.");
    }
}
