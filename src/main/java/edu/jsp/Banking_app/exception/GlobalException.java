package edu.jsp.Banking_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<String> NotfoundExceptionHandler(
            NotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = InsufficientBalanceException.class)
    public ResponseEntity<String> InsufficientBalanceHandler(
            InsufficientBalanceException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<String> invalidCredentialsHandler(
            InvalidCredentialsException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED
        );
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<String> accessDeniedHandler(
            AccessDeniedException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }
    @ExceptionHandler(AccountCannotBeClosedException.class)
    public ResponseEntity<String> handleAccountCannotBeClosed(
            AccountCannotBeClosedException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<String> handleAccountNotActive(
            AccountNotActiveException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}