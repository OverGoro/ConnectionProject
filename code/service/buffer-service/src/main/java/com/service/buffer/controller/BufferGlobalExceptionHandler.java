// GlobalExceptionHandler.java
package com.service.buffer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.exception.BufferValidateException;

@Slf4j
@RestControllerAdvice
public class BufferGlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException e) {
        log.warn("Security exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                "invalid_authorization",
                "Invalid or missing authorization header"));
    }

    @ExceptionHandler(BufferNotFoundException.class)
    public ResponseEntity<?> handleBufferNotFoundException(
            BufferNotFoundException e) {
        log.warn("Buffer not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "buffer_not_found",
                "Buffer not found"));
    }

    @ExceptionHandler(BufferValidateException.class)
    public ResponseEntity<?> handleBufferValidationException(
            BufferValidateException e) {
        log.warn("Buffer validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "validation_failed",
                e.getMessage() != null ? e.getMessage() : "Invalid buffer data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "internal_server_error",
                "An unexpected error occurred"));
    }
}