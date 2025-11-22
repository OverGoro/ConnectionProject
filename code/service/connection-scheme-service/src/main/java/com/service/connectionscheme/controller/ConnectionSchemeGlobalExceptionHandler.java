
package com.service.connectionscheme.controller;

import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.exception.ConnectionSchemeValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** . */
@Slf4j
@RestControllerAdvice
public class ConnectionSchemeGlobalExceptionHandler {
    /** . */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException e) {
        log.warn("Security exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("invalid_authorization",
                        "Invalid or missing authorization header"));
    }

    /** . */
    @ExceptionHandler(ConnectionSchemeNotFoundException.class)
    public ResponseEntity<?> handleConnectionSchemeNotFoundException(
            ConnectionSchemeNotFoundException e) {
        log.warn("Connection scheme not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("connection_scheme_not_found",
                        "Connection scheme not found"));
    }

    /** . */
    @ExceptionHandler(ConnectionSchemeValidateException.class)
    public ResponseEntity<?> handleConnectionSchemeValidationException(
            ConnectionSchemeValidateException e) {
        log.warn("Connection scheme validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("validation_failed",
                        e.getMessage() != null ? e.getMessage()
                                : "Invalid connection scheme data"));
    }

    /** . */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("internal_server_error",
                        "An unexpected error occurred"));
    }
}
