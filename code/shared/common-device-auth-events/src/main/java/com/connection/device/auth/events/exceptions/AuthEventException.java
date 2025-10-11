package com.connection.device.auth.events.exceptions;

public class AuthEventException extends RuntimeException {
    
    public AuthEventException(String message) {
        super(message);
    }
    
    public AuthEventException(String message, Throwable cause) {
        super(message, cause);
    }
}