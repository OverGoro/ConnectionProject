// BufferDeviceValidator.java
package com.connection.processing.buffer.bufferdevice.validator;

import java.util.UUID;

import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceValidateException;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

public class BufferDeviceValidator {
    public void validate(BufferDeviceDTO bufferDevice) {
        if (bufferDevice == null) {
            throw new BufferDeviceValidateException("null", "null", "BufferDevice is null");
        }
        try {
            validateBufferUid(bufferDevice.getBufferUid());
            validateDeviceUid(bufferDevice.getDeviceUid());
        } catch (IllegalArgumentException e) {
            throw new BufferDeviceValidateException(bufferDevice.getBufferUid(), bufferDevice.getDeviceUid(),
                    e.getMessage());
        }
    }

    public void validate(BufferDeviceBLM bufferDevice) {
        if (bufferDevice == null) {
            throw new BufferDeviceValidateException("null", "null", "BufferDevice is null");
        }
        try {
            validateBufferUid(bufferDevice.getBufferUid());
            validateDeviceUid(bufferDevice.getDeviceUid());
        } catch (IllegalArgumentException e) {
            throw new BufferDeviceValidateException(
                    bufferDevice.getBufferUid() != null ? bufferDevice.getBufferUid().toString() : "null",
                    bufferDevice.getDeviceUid() != null ? bufferDevice.getDeviceUid().toString() : "null",
                    e.getMessage());
        }
    }

    public void validate(BufferDeviceDALM bufferDevice) {
        if (bufferDevice == null) {
            throw new BufferDeviceValidateException("null", "null", "BufferDevice is null");
        }
        try {
            validateBufferUid(bufferDevice.getBufferUid());
            validateDeviceUid(bufferDevice.getDeviceUid());
        } catch (IllegalArgumentException e) {
            throw new BufferDeviceValidateException(
                    bufferDevice.getBufferUid() != null ? bufferDevice.getBufferUid().toString() : "null",
                    bufferDevice.getDeviceUid() != null ? bufferDevice.getDeviceUid().toString() : "null", e.getMessage());
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

    private void validateDeviceUid(UUID deviceUid) {
        if (deviceUid == null) {
            throw new IllegalArgumentException("Device UID cannot be null");
        }
    }

    private void validateDeviceUid(String deviceUid) {
        if (deviceUid == null || deviceUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Device UID cannot be empty");
        }
        try {
            UUID.fromString(deviceUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Device UID format");
        }
    }
}