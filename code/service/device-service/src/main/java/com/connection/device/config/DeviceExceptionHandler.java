// DeviceExceptionHandler.java
package com.connection.device.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.connection.device.exception.BaseDeviceException;
import com.connection.device.exception.DeviceAddException;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.exception.DeviceValidateException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class DeviceExceptionHandler {

    @ExceptionHandler(DeviceAddException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceAddException(DeviceAddException ex) {
        return createErrorResponse(
            "DEVICE_ADD_ERROR",
            "Cannot add device",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceAlreadyExistsException(DeviceAlreadyExistsException ex) {
        return createErrorResponse(
            "DEVICE_ALREADY_EXISTS",
            "Device already exists",
            ex.getMessage(),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return createErrorResponse(
            "DEVICE_NOT_FOUND",
            "Device not found",
            ex.getMessage(),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DeviceValidateException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceValidateException(DeviceValidateException ex) {
        return createErrorResponse(
            "DEVICE_VALIDATION_ERROR",
            "Device validation failed",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BaseDeviceException.class)
    public ResponseEntity<Map<String, Object>> handleBaseDeviceException(BaseDeviceException ex) {
        return createErrorResponse(
            "DEVICE_ERROR",
            "Device operation failed",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            String errorCode, 
            String errorMessage, 
            String details, 
            HttpStatus status) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("code", errorCode);
        errorResponse.put("message", errorMessage);
        errorResponse.put("details", details);
        
        return new ResponseEntity<>(errorResponse, status);
    }
}