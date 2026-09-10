package edu.jsp.Banking_app.exception;

public class AccountCannotBeClosedException extends RuntimeException {

    public AccountCannotBeClosedException(String message) {
        super(message);
    }
}