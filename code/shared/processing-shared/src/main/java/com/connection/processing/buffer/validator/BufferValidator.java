// BufferValidator.java
package com.connection.processing.buffer.validator;

import java.util.UUID;

import com.connection.processing.buffer.exception.BufferValidateException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;

public class BufferValidator {
    public void validate(BufferDTO buffer) {
        if (buffer == null) {
            throw new BufferValidateException("null", "Buffer is null");
        }
        try {
            validateUid(buffer.getUid());
            validateConnectionSchemeUid(buffer.getConnectionSchemeUid());
        } catch (IllegalArgumentException e) {
            throw new BufferValidateException(buffer.getUid(), e.getMessage());
        }
    }

    public void validate(BufferBLM buffer) {
        if (buffer == null) {
            throw new BufferValidateException("null", "Buffer is null");
        }
        try {
            validateUid(buffer.getUid());
            validateConnectionSchemeUid(buffer.getConnectionSchemeUid());
        } catch (IllegalArgumentException e) {
            throw new BufferValidateException(buffer.getUid().toString(), e.getMessage());
        }
    }

    public void validate(BufferDALM buffer) {
        if (buffer == null) {
            throw new BufferValidateException("null", "Buffer is null");
        }
        try {
            validateUid(buffer.getUid());
            validateConnectionSchemeUid(buffer.getConnectionSchemeUid());
        } catch (IllegalArgumentException e) {
            throw new BufferValidateException(buffer.getUid().toString(), e.getMessage());
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

    private void validateConnectionSchemeUid(String connectionSchemeUid) {
        if (connectionSchemeUid == null || connectionSchemeUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Connection scheme UID cannot be empty");
        }
        try {
            UUID.fromString(connectionSchemeUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Connection scheme UID format");
        }
    }

    private void validateConnectionSchemeUid(UUID connectionSchemeUid) {
        if (connectionSchemeUid == null) {
            throw new IllegalArgumentException("Connection scheme UID cannot be null");
        }
    }
}