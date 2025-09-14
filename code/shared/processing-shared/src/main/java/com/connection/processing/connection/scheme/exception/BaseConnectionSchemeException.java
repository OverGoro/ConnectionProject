// BaseConnectionSchemeException.java
package com.connection.processing.connection.scheme.exception;

public class BaseConnectionSchemeException extends RuntimeException {
    private final String schemeUid;

    public BaseConnectionSchemeException(String schemeUid) {
        super("connection scheme");
        this.schemeUid = schemeUid;
    }

    public String toString() {
        return super.toString() + "\nscheme: " + schemeUid;
    }
}