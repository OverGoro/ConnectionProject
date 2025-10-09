// DeviceAccessTokenValidator.java
package com.connection.device.token.validator;

import java.util.Date;

import com.connection.device.token.exception.DeviceAccessTokenValidateException;
import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

public class DeviceAccessTokenValidator {
    
    private static final int MAX_TOKEN_LENGTH = 512;
    
    public void validate(DeviceAccessTokenDTO deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device access token is null");
        }
        validateToken(deviceToken.getToken());
    }

    public void validate(DeviceAccessTokenBLM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device access token is null");
        }
        validateToken(deviceToken.getToken());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    public void validate(DeviceAccessTokenDALM deviceToken) {
        if (deviceToken == null) {
            throw new DeviceAccessTokenValidateException("null", "Device access token is null");
        }
        validateUid(deviceToken.getUid());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
        validateToken(deviceToken.getToken());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new DeviceAccessTokenValidateException("", "Token cannot be empty");
        }
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw new DeviceAccessTokenValidateException("","Token exceeds maximum length of " + MAX_TOKEN_LENGTH + " characters");
        }
    }

    private void validateUid(java.util.UUID uid) {
        if (uid == null) {
            throw new DeviceAccessTokenValidateException("","UID cannot be null");
        }
    }

    private void validateDeviceTokenUid(java.util.UUID deviceTokenUid) {
        if (deviceTokenUid == null) {
            throw new DeviceAccessTokenValidateException("","Device token UID cannot be null");
        }
    }

    private void validateDates(Date createdAt, Date expiresAt) {
        if (createdAt == null) {
            throw new DeviceAccessTokenValidateException("","Creation date cannot be null");
        }
        if (expiresAt == null) {
            throw new DeviceAccessTokenValidateException("","Expiration date cannot be null");
        }
        
        Date now = new Date();
        
        if (expiresAt.before(now)) {
            throw new DeviceAccessTokenValidateException("","Token has already expired");
        }
        if (expiresAt.before(createdAt)) {
            throw new DeviceAccessTokenValidateException("","Expiration date cannot be before creation date");
        }
        if (createdAt.after(now)){
            throw new DeviceTokenValidateException("","Creation date cannot be before creation date");
        }
    }
}