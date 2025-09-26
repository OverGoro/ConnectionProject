// DeviceAccessTokenValidator.java
package com.connection.device.token.validator;

import java.util.Date;

import com.connection.device.token.exception.DeviceAccessTokenValidateException;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

public class DeviceAccessTokenValidator {
    
    public void validate(DeviceAccessTokenDTO deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device token is null");
        }
        validateToken(deviceToken.getToken());
    }

    public void validate(DeviceAccessTokenBLM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device token is null");
        }
        validateToken(deviceToken.getToken());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    public void validate(DeviceAccessTokenDALM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device token is null");
        }
        validateUid(deviceToken.getUid());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
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

    private void validateDeviceTokenUid(java.util.UUID deviceUid) {
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