package com.connection.service.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.connection.service.auth.controller.model.ErrorResponse;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(com.connection.client.exception.ClientAlreadyExisistsException.class)
    public ResponseEntity<?> handleClientAlreadyExistsException(
            com.connection.client.exception.ClientAlreadyExisistsException e) {
        log.warn("Client registration failed: {}", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "client_already_exist",
                "An account with such email or uid already exists"));
    }

    @ExceptionHandler(com.connection.client.exception.ClientNotFoundException.class)
    public ResponseEntity<?> handleClientNotFoundException(com.connection.client.exception.ClientNotFoundException e) {
        log.warn("Client not found: {}", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "client_not_found",
                "User not found"));
    }

    @ExceptionHandler(com.connection.client.exception.ClientValidateException.class)
    public ResponseEntity<?> handleClientValidateException(com.connection.client.exception.ClientValidateException e) {
        log.warn("Client validation failed: {}\n {}", e, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "validation_failed",
                e.getMessage() != null ? e.getMessage() : "Invalid user data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "An unexpected error occurred"));
    }
}