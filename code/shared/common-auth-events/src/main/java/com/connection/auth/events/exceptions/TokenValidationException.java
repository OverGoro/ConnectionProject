package com.connection.auth.events.exceptions;

/** . */
public class TokenValidationException extends AuthEventException {

    /** . */
    public TokenValidationException(String message) {
        super(message);
    }

    /** . */
    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
