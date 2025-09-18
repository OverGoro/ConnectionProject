// ConnectionSchemeValidator.java
package com.connection.scheme.validator;

import java.util.UUID;

import com.connection.scheme.exception.ConnectionSchemeValidateException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeValidator {
    public void validate(ConnectionSchemeDTO scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid(), e.getMessage());
        }
    }

    public void validate(ConnectionSchemeBLM scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid().toString(), e.getMessage());
        }
    }

    public void validate(ConnectionSchemeDALM scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid().toString(), e.getMessage());
        }
    }

    private void validateUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID cannot be empty");
        }
        try {
            UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UID format");
        }
    }

    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID cannot be null");
        }
    }

    private void validateClientUid(String clientUid) {
        if (clientUid == null || clientUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Client UID cannot be empty");
        }
        try {
            UUID.fromString(clientUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Client UID format");
        }
    }

    private void validateClientUid(UUID clientUid) {
        if (clientUid == null) {
            throw new IllegalArgumentException("Client UID cannot be null");
        }
    }

    private void validateSchemeJson(String schemeJson) {
        if (schemeJson == null || schemeJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheme JSON cannot be empty");
        }
        if (!schemeJson.trim().startsWith("{")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }
}