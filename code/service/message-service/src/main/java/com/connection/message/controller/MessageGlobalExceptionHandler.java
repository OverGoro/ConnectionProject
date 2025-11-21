package com.connection.message.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** . */
@Slf4j
@RestControllerAdvice
public class MessageGlobalExceptionHandler {
    /** . */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException e) {
        log.warn("Security exception: {}", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("access_denied", e.getMessage()));
    }

    /** . */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleAuthException(
            AuthenticationCredentialsNotFoundException e) {
        log.warn("Authentication required: {}", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("authentication_required",
                        "Client or device authentication required"));
    }

    /** . */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(
            AccessDeniedException e) {
        log.warn("Access denied: {}", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse("access_denied", "Insufficient permissions"));
    }

    /** . */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("internal_server_error",
                        "An unexpected error occurred"));
    }

    /** . */
    @AllArgsConstructor
    @Getter
    public static class ErrorResponse {
        private final String error;
        private final String message;
    }
}
