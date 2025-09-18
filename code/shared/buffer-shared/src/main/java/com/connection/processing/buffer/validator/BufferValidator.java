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
            validateMaxMessagesNumber(buffer.getMaxMessagesNumber());
            validateMaxMessageSize(buffer.getMaxMessageSize());
            validateMessagePrototype(buffer.getMessagePrototype());
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
            validateMaxMessagesNumber(buffer.getMaxMessagesNumber());
            validateMaxMessageSize(buffer.getMaxMessageSize());
            validateMessagePrototype(buffer.getMessagePrototype());
        } catch (IllegalArgumentException e) {
            throw new BufferValidateException(buffer.getUid() != null ? buffer.getUid().toString() : "null", e.getMessage());
        }
    }

    public void validate(BufferDALM buffer) {
        if (buffer == null) {
            throw new BufferValidateException("null", "Buffer is null");
        }
        try {
            validateUid(buffer.getUid());
            validateConnectionSchemeUid(buffer.getConnectionSchemeUid());
            validateMaxMessagesNumber(buffer.getMaxMessagesNumber());
            validateMaxMessageSize(buffer.getMaxMessageSize());
            validateMessagePrototype(buffer.getMessagePrototype());
        } catch (IllegalArgumentException e) {
            throw new BufferValidateException(buffer.getUid() != null ? buffer.getUid().toString() : "null", e.getMessage());
        }
    }

    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID cannot be null");
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

    private void validateConnectionSchemeUid(UUID connectionSchemeUid) {
        if (connectionSchemeUid == null) {
            throw new IllegalArgumentException("Connection Scheme UID cannot be null");
        }
    }

    private void validateConnectionSchemeUid(String connectionSchemeUid) {
        if (connectionSchemeUid == null || connectionSchemeUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Connection Scheme UID cannot be empty");
        }
        try {
            UUID.fromString(connectionSchemeUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Connection Scheme UID format");
        }
    }

    private void validateMaxMessagesNumber(Integer maxMessagesNumber) {
        if (maxMessagesNumber == null) {
            throw new IllegalArgumentException("Max messages number cannot be null");
        }
        if (maxMessagesNumber <= 0) {
            throw new IllegalArgumentException("Max messages number must be greater than 0");
        }
    }

    private void validateMaxMessageSize(Integer maxMessageSize) {
        if (maxMessageSize == null) {
            throw new IllegalArgumentException("Max message size cannot be null");
        }
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("Max message size must be greater than 0");
        }
    }

    private void validateMessagePrototype(String messagePrototype) {
        if (messagePrototype == null || messagePrototype.trim().isEmpty()) {
            throw new IllegalArgumentException("Message prototype cannot be empty");
        }
    }
}