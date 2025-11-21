
package com.connection.processing.buffer.validator;

import com.connection.processing.buffer.exception.BufferValidateException;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;
import java.util.UUID;

/** . */
public class BufferValidator {
    /** . */
    public void validate(BufferDto buffer) {
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

    /** . */
    public void validate(BufferBlm buffer) {
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
            throw new BufferValidateException(
                    buffer.getUid() != null ? buffer.getUid().toString()
                            : "null",
                    e.getMessage());
        }
    }

    /** . */
    public void validate(BufferDalm buffer) {
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
            throw new BufferValidateException(
                    buffer.getUid() != null ? buffer.getUid().toString()
                            : "null",
                    e.getMessage());
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
            throw new IllegalArgumentException(
                    "Max messages number cannot be null");
        }
        if (maxMessagesNumber <= 0) {
            throw new IllegalArgumentException(
                    "Max messages number must be greater than 0");
        }
    }

    private void validateMaxMessageSize(Integer maxMessageSize) {
        if (maxMessageSize == null) {
            throw new IllegalArgumentException(
                    "Max message size cannot be null");
        }
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException(
                    "Max message size must be greater than 0");
        }
    }

    private void validateMessagePrototype(String messagePrototype) {
        if (messagePrototype == null || messagePrototype.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Message prototype cannot be empty");
        }
    }
}
