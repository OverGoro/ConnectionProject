package com.connection.device.token.validator;

import com.connection.device.token.exception.DeviceAccessTokenValidateException;
import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.model.DeviceAccessTokenDto;
import java.util.Date;
import java.util.UUID;

/** . */
public class DeviceAccessTokenValidator {

    private static final int MAX_TOKEN_LENGTH = 512;

    /** . */
    public void validate(DeviceAccessTokenDto deviceToken) {
        validateNotNull(deviceToken, "Device access token");
        validateToken(deviceToken.getToken());
    }

    /** . */
    public void validate(DeviceAccessTokenBlm deviceToken) {
        validateNotNull(deviceToken, "Device access token");
        validateToken(deviceToken.getToken());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    /** . */
    public void validate(DeviceAccessTokenDalm deviceToken) {
        validateNotNull(deviceToken, "Device access token");
        validateUid(deviceToken.getUid());
        validateDeviceTokenUid(deviceToken.getDeviceTokenUid());
        validateToken(deviceToken.getToken());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    private void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new DeviceAccessTokenValidateException("null",
                    fieldName + " is null");
        }
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new DeviceAccessTokenValidateException("",
                    "Token cannot be empty");
        }
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw new DeviceAccessTokenValidateException("",
                    "Token exceeds maximum length of " + MAX_TOKEN_LENGTH
                            + " characters");
        }
    }

    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new DeviceAccessTokenValidateException("",
                    "UID cannot be null");
        }
    }

    private void validateDeviceTokenUid(UUID deviceTokenUid) {
        if (deviceTokenUid == null) {
            throw new DeviceAccessTokenValidateException("",
                    "Device token UID cannot be null");
        }
    }

    private void validateDates(Date createdAt, Date expiresAt) {
        validateDateNotNull(createdAt, "Creation date");
        validateDateNotNull(expiresAt, "Expiration date");

        Date now = new Date();
        validateDateOrder(createdAt, expiresAt, now);
    }

    private void validateDateNotNull(Date date, String dateName) {
        if (date == null) {
            throw new DeviceAccessTokenValidateException("",
                    dateName + " cannot be null");
        }
    }

    private void validateDateOrder(Date createdAt, Date expiresAt, Date now) {
        if (expiresAt.before(now)) {
            throw new DeviceAccessTokenValidateException("",
                    "Token has already expired");
        }
        if (expiresAt.before(createdAt)) {
            throw new DeviceAccessTokenValidateException("",
                    "Expiration date cannot be before creation date");
        }
        if (createdAt.after(now)) {
            throw new DeviceTokenValidateException("",
                    "Creation date cannot be in the future");
        }
    }
}
