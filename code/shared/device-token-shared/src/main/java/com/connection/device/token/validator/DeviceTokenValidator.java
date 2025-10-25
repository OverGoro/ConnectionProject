// DeviceTokenValidator.java
package com.connection.device.token.validator;

import java.util.Date;

import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

public class DeviceTokenValidator {
    
    private static final int MAX_TOKEN_LENGTH = 512;
    
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
        validateToken(deviceToken.getToken());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new DeviceTokenValidateException("","Token cannot be empty");
        }
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw new DeviceTokenValidateException("","Token exceeds maximum length of " + MAX_TOKEN_LENGTH + " characters");
        }
    }

    private void validateUid(java.util.UUID uid) {
        if (uid == null) {
            throw new DeviceTokenValidateException("","UID cannot be null");
        }
    }

    private void validateDeviceUid(java.util.UUID deviceUid) {
        if (deviceUid == null) {
            throw new DeviceTokenValidateException("","Device UID cannot be null");
        }
    }

    private void validateDates(Date createdAt, Date expiresAt) {
        if (createdAt == null) {
            throw new DeviceTokenValidateException("","Creation date cannot be null");
        }
        if (expiresAt == null) {
            throw new DeviceTokenValidateException("","Expiration date cannot be null");
        }
        
        Date now = new Date();
        
        if (expiresAt.before(now)) {
            throw new DeviceTokenValidateException("","Token has already expired");
        }
        if (expiresAt.before(createdAt)) {
            throw new DeviceTokenValidateException("","Expiration date cannot be before creation date");
        }
        if (createdAt.after(now)){
            throw new DeviceTokenValidateException("","Creation date cannot be before creation date");
        }
    }
}