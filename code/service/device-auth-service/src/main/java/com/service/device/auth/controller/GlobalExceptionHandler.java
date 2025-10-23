package com.service.device.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.connection.device.exception.DeviceAlreadyExistsException.class)
    public ResponseEntity<?> handleDeviceAlreadyExistsException(
            com.connection.device.exception.DeviceAlreadyExistsException e) {
        log.warn("Device registration failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "device_already_exist",
                "An device with such uid already exists"));
    }

    @ExceptionHandler(com.connection.device.exception.DeviceNotFoundException.class)
    public ResponseEntity<?> handleDeviceNotFoundException(com.connection.device.exception.DeviceNotFoundException e) {
        log.warn("Device not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "device_not_found",
                "User not found"));
    }

    @ExceptionHandler(com.connection.device.exception.DeviceValidateException.class)
    public ResponseEntity<?> handleDeviceValidateException(com.connection.device.exception.DeviceValidateException e) {
        log.warn("Device validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "validation_failed",
                e.getMessage() != null ? e.getMessage() : "Invalid device data"));
    }




    @ExceptionHandler(com.connection.device.token.exception.DeviceTokenAlreadyExistsException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceTokenAlreadyExistsException e){
        log.warn("Device token adding failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "device_token_already_exist",
                "Token for this device already exists"));

    }

    @ExceptionHandler(com.connection.device.token.exception.DeviceTokenNotFoundException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceTokenNotFoundException e){
        log.warn("Device token getting failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "device_token_not_found",
                "No such token exist"));
    }

    @ExceptionHandler(com.connection.device.token.exception.DeviceTokenValidateException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceTokenValidateException e){
        log.warn("Device token getting failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "device_token_invalid",
                "Invalid token"));
    }





    @ExceptionHandler(com.connection.device.token.exception.DeviceAccessTokenExistsException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceAccessTokenExistsException e){
        log.warn("Device access token adding failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "device_access_token_already_exist",
                "Access token for this device already exists"));

    }

    @ExceptionHandler(com.connection.device.token.exception.DeviceAccessTokenNotFoundException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceAccessTokenNotFoundException e){
        log.warn("Device access token getting failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "device_access_token_not_found",
                "No such access token exist"));
    }

    @ExceptionHandler(com.connection.device.token.exception.DeviceAccessTokenValidateException.class)
    public ResponseEntity<?> handeleDeviceTokenException(com.connection.device.token.exception.DeviceAccessTokenValidateException e){
        log.warn("Device token getting failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "device_access_token_invalid",
                "Invalid access token"));
    }
    
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<?> handeleJwtException(io.jsonwebtoken.JwtException e){
        log.warn("JWT token invalid: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                "jwt_token_invalid",
                "Invalid jwt token"));
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<?> handeleJwtException(IllegalArgumentException e){
        log.warn("JWT token invalid: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "illegal_argument",
                "Illegal argument"));
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "An unexpected error occurred"));
    }
}