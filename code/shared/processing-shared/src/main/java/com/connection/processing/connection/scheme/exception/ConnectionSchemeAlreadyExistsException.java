// ConnectionSchemeAlreadyExistsException.java
package com.connection.processing.connection.scheme.exception;

public class ConnectionSchemeAlreadyExistsException extends BaseConnectionSchemeException {
    public ConnectionSchemeAlreadyExistsException(String schemeUid) {
        super(schemeUid);
    }

    public String toString() {
        return super.toString() + "\ndescription: connection scheme already exists";
    }
}