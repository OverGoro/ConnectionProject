// DeviceTokenValidator.java
package com.connection.device.token.validator;

import java.util.Date;

import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

public class DeviceTokenValidator {
    
    public void validate(DeviceTokenDTO deviceToken) {
        if (deviceToken == null) {
            throw new DeviceTokenValidateException("null", "Device token is null");
        }
        validateToken(deviceToken.getToken());
    }

    public void validate(DeviceTokenBLM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceTokenValidateException("null", "Device token is null");
        }
        validateToken(deviceToken.getToken());
        validateDeviceUid(deviceToken.getDeviceUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    public void validate(DeviceTokenDALM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceTokenValidateException("null", "Device token is null");
        }
        validateUid(deviceToken.getUid());
        validateDeviceUid(deviceToken.getDeviceUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }
        if (token.length() > 512) {
            throw new IllegalArgumentException("Token exceeds maximum length");
        }
    }

    private void validateUid(java.util.UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID cannot be null");
        }
    }

    private void validateDeviceUid(java.util.UUID deviceUid) {
        if (deviceUid == null) {
            throw new IllegalArgumentException("Device UID cannot be null");
        }
    }

    private void validateDates(Date createdAt, Date expiresAt) {
        if (createdAt == null || expiresAt == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        
        Date now = new Date();
        if (createdAt.after(now)) {
            throw new IllegalArgumentException("Creation date cannot be in the future");
        }
        if (expiresAt.before(now)) {
            throw new IllegalArgumentException("Token has already expired");
        }
        if (expiresAt.before(createdAt)) {
            throw new IllegalArgumentException("Expiration date cannot be before creation date");
        }
    }
}