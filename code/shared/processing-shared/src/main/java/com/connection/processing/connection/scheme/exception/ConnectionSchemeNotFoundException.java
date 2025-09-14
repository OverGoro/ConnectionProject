// ConnectionSchemeNotFoundException.java
package com.connection.processing.connection.scheme.exception;

public class ConnectionSchemeNotFoundException extends BaseConnectionSchemeException {
    public ConnectionSchemeNotFoundException(String schemeUid) {
        super(schemeUid);
    }

    public String toString() {
        return super.toString() + "\ndescription: connection scheme not found";
    }
}