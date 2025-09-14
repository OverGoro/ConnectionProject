// BufferJsonObjectValidator.java
package com.connection.processing.buffer.objects.json.validator;

import java.util.UUID;

import com.connection.processing.buffer.objects.json.exception.BufferJsonObjectValidateException;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDTO;

public class BufferJsonObjectValidator {
    public void validate(BufferJsonObjectDTO object) {
        if (object == null) {
            throw new BufferJsonObjectValidateException("null", "Object is null");
        }
        try {
            validateUid(object.getUid());
            validateBufferUid(object.getBufferUid());
            validateData(object.getData());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonObjectValidateException(object.getUid(), e.getMessage());
        }
    }

    public void validate(BufferJsonObjectBLM object) {
        if (object == null) {
            throw new BufferJsonObjectValidateException("null", "Object is null");
        }
        try {
            validateUid(object.getUid());
            validateBufferUid(object.getBufferUid());
            validateData(object.getData());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonObjectValidateException(object.getUid().toString(), e.getMessage());
        }
    }

    public void validate(BufferJsonObjectDALM object) {
        if (object == null) {
            throw new BufferJsonObjectValidateException("null", "Object is null");
        }
        try {
            validateUid(object.getUid());
            validateBufferUid(object.getBufferUid());
            validateData(object.getData());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonObjectValidateException(object.getUid().toString(), e.getMessage());
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

    private void validateBufferUid(String bufferUid) {
        if (bufferUid == null || bufferUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Buffer UID cannot be empty");
        }
        try {
            UUID.fromString(bufferUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Buffer UID format");
        }
    }

    private void validateBufferUid(UUID bufferUid) {
        if (bufferUid == null) {
            throw new IllegalArgumentException("Buffer UID cannot be null");
        }
    }

    private void validateData(String data) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
        if (!data.trim().startsWith("{")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }
}