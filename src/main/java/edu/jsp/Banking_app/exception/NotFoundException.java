package edu.jsp.Banking_app.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(
            String message,
            String field,
            Object value) {

        super(message + " | " + field + ": " + value);
    }
}