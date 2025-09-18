// DeviceValidator.java
package com.connection.device.validator;

import java.util.UUID;

import com.connection.device.exception.DeviceValidateException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

public class DeviceValidator {
    public void validate(DeviceDTO device) {
        if (device == null) {
            throw new DeviceValidateException("null", "Device is null");
        }
        try {
            validateUid(device.getUid());
            validateClientUuid(device.getClientUuid());
            validateDeviceName(device.getDeviceName());
            validateDeviceDescription(device.getDeviceDescription());
        } catch (IllegalArgumentException e) {
            throw new DeviceValidateException(device.getUid(), e.getMessage());
        }
    }

    public void validate(DeviceBLM device) {
        if (device == null) {
            throw new DeviceValidateException("null", "Device is null");
        }
        try {
            validateUid(device.getUid());
            validateClientUuid(device.getClientUuid());
            validateDeviceName(device.getDeviceName());
            validateDeviceDescription(device.getDeviceDescription());
        } catch (IllegalArgumentException e) {
            throw new DeviceValidateException(device.getUid() != null ? device.getUid().toString() : "null", e.getMessage());
        }
    }

    public void validate(DeviceDALM device) {
        if (device == null) {
            throw new DeviceValidateException("null", "Device is null");
        }
        try {
            validateUid(device.getUid());
            validateClientUuid(device.getClientUuid());
            validateDeviceName(device.getDeviceName());
            validateDeviceDescription(device.getDeviceDescription());
        } catch (IllegalArgumentException e) {
            throw new DeviceValidateException(device.getUid() != null ? device.getUid().toString() : "null", e.getMessage());
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

    private void validateClientUuid(UUID clientUuid) {
        if (clientUuid == null) {
            throw new IllegalArgumentException("Client UUID cannot be null");
        }
    }

    private void validateClientUuid(String clientUuid) {
        if (clientUuid == null || clientUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("Client UUID cannot be empty");
        }
        try {
            UUID.fromString(clientUuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Client UUID format");
        }
    }

    private void validateDeviceName(String deviceName) {
        if (deviceName == null || deviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be empty");
        }
        if (deviceName.length() > 100) {
            throw new IllegalArgumentException("Device name cannot exceed 100 characters");
        }
    }

    private void validateDeviceDescription(String deviceDescription) {
        if (deviceDescription == null || deviceDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Device description cannot be empty");
        }
        if (deviceDescription.length() > 500) {
            throw new IllegalArgumentException("Device description cannot exceed 500 characters");
        }
    }
}