// BufferJsonDataValidator.java
package com.connection.processing.buffer.objects.json.validator;

import java.time.Instant;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.exception.BufferJsonDataValidateException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

public class BufferJsonDataValidator {
    public void validate(BufferJsonDataDTO data) {
        if (data == null) {
            throw new BufferJsonDataValidateException("null", "Buffer JSON data is null");
        }
        try {
            validateUid(data.getUid());
            validateBufferUid(data.getBufferUid());
            validateData(data.getData());
            validateCreatedAt(data.getCreatedAt());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonDataValidateException(data.getUid(), e.getMessage());
        }
    }

    public void validate(BufferJsonDataBLM data) {
        if (data == null) {
            throw new BufferJsonDataValidateException("null", "Buffer JSON data is null");
        }
        try {
            validateUid(data.getUid());
            validateBufferUid(data.getBufferUid());
            validateData(data.getData());
            validateCreatedAt(data.getCreatedAt());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonDataValidateException(data.getUid() != null ? data.getUid().toString() : "null", e.getMessage());
        }
    }

    public void validate(BufferJsonDataDALM data) {
        if (data == null) {
            throw new BufferJsonDataValidateException("null", "Buffer JSON data is null");
        }
        try {
            validateUid(data.getUid());
            validateBufferUid(data.getBufferUid());
            validateData(data.getData());
            validateCreatedAt(data.getCreatedAt());
        } catch (IllegalArgumentException e) {
            throw new BufferJsonDataValidateException(data.getUid() != null ? data.getUid().toString() : "null", e.getMessage());
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

    private void validateBufferUid(UUID bufferUid) {
        if (bufferUid == null) {
            throw new IllegalArgumentException("Buffer UID cannot be null");
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

    private void validateData(String data) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON data cannot be empty");
        }
        // Можно добавить дополнительную валидацию JSON формата
        try {
            // Простая проверка что это похоже на JSON
            if (!data.trim().startsWith("{") || !data.trim().endsWith("}")) {
                throw new IllegalArgumentException("Data must be a valid JSON object");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }

    private void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Creation timestamp cannot be null");
        }
        
        Instant now = Instant.now();
        if (createdAt.isAfter(now)) {
            throw new IllegalArgumentException("Creation timestamp cannot be in the future");
        }
    }
}