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
            validateDeviceUid(buffer.getDeviceUid()); // Изменено
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
            validateDeviceUid(buffer.getDeviceUid()); // Изменено
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
            validateDeviceUid(buffer.getDeviceUid()); // Изменено
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

    private void validateDeviceUid(UUID deviceUid) { // Новый метод
        if (deviceUid == null) {
            throw new IllegalArgumentException("Device UID cannot be null");
        }
    }

    private void validateDeviceUid(String deviceUid) { // Новый метод
        if (deviceUid == null || deviceUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Device UID cannot be empty");
        }
        try {
            UUID.fromString(deviceUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Device UID format");
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