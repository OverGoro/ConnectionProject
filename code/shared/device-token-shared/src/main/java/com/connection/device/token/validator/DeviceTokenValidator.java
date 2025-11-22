package com.connection.device.token.validator;

import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.model.DeviceTokenDto;
import java.util.Date;
import java.util.UUID;

/** . */
public class DeviceTokenValidator {

    private static final int MAX_TOKEN_LENGTH = 512;

    /** . */
    public void validate(DeviceTokenDto deviceToken) {
        validateNotNull(deviceToken, "Device token");
        validateToken(deviceToken.getToken());
    }

    /** . */
    public void validate(DeviceTokenBlm deviceToken) {
        validateNotNull(deviceToken, "Device token");
        validateToken(deviceToken.getToken());
        validateDeviceUid(deviceToken.getDeviceUid());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    /** . */
    public void validate(DeviceTokenDalm deviceToken) {
        validateNotNull(deviceToken, "Device token");
        validateUid(deviceToken.getUid());
        validateDeviceUid(deviceToken.getDeviceUid());
        validateToken(deviceToken.getToken());
        validateDates(deviceToken.getCreatedAt(), deviceToken.getExpiresAt());
    }

    private void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new DeviceTokenValidateException("null",
                    fieldName + " is null");
        }
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new DeviceTokenValidateException("", "Token cannot be empty");
        }
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw new DeviceTokenValidateException("",
                    "Token exceeds maximum length of " + MAX_TOKEN_LENGTH
                            + " characters");
        }
    }

    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new DeviceTokenValidateException("", "UID cannot be null");
        }
    }

    private void validateDeviceUid(UUID deviceUid) {
        if (deviceUid == null) {
            throw new DeviceTokenValidateException("",
                    "Device UID cannot be null");
        }
    }

    private void validateDates(Date createdAt, Date expiresAt) {
        validateDateNotNull(createdAt, "Creation date");
        validateDateNotNull(expiresAt, "Expiration date");
        validateDateLogic(createdAt, expiresAt);
    }

    private void validateDateNotNull(Date date, String dateName) {
        if (date == null) {
            throw new DeviceTokenValidateException("",
                    dateName + " cannot be null");
        }
    }

    private void validateDateLogic(Date createdAt, Date expiresAt) {
        Date now = new Date();

        validateNotExpired(expiresAt, now);
        validateDateOrder(createdAt, expiresAt);
        validateCreationDateNotInFuture(createdAt, now);
    }

    private void validateNotExpired(Date expiresAt, Date now) {
        if (expiresAt.before(now)) {
            throw new DeviceTokenValidateException("",
                    "Token has already expired");
        }
    }

    private void validateDateOrder(Date createdAt, Date expiresAt) {
        if (expiresAt.before(createdAt)) {
            throw new DeviceTokenValidateException("",
                    "Expiration date cannot be before creation date");
        }
    }

    private void validateCreationDateNotInFuture(Date createdAt, Date now) {
        if (createdAt.after(now)) {
            throw new DeviceTokenValidateException("",
                    "Creation date cannot be in the future");
        }
    }
}
