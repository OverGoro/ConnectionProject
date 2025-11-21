package com.connection.token.validator;

import java.util.Date;
import java.util.UUID;

import com.connection.token.exception.AccessTokenValidateException;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import com.connection.token.model.AccessTokenDto;

public class AccessTokenValidator {
    public void validate(AccessTokenDto accessToken) {
        if (accessToken == null) {
            throw new AccessTokenValidateException("null", "Access token is null");
        }
        try {
            validateToken(accessToken.getToken());
        } catch (IllegalArgumentException e) {
            throw new AccessTokenValidateException("accessToken", e.getMessage());
        }
    }

    public void validate(AccessTokenBlm accessToken) {
        if (accessToken == null) {
            throw new AccessTokenValidateException("null", "Access token is null");
        }
        try {
            validateToken(accessToken.getToken());
            validateClientUid(accessToken.getClientUid());
            validateCreatedAt(accessToken.getCreatedAt());
            validateExpiresAt(accessToken.getExpiresAt());
        } catch (IllegalArgumentException e) {
            throw new AccessTokenValidateException("accessToken", e.getMessage());
        }
    }

    public void validate(AccessTokenDalm accessToken) {
        if (accessToken == null) {
            throw new AccessTokenValidateException("null", "Access token is null");
        }
        try {
            validateClientUid(accessToken.getClientUid());
            validateCreatedAt(accessToken.getCreatedAt());
            validateExpiresAt(accessToken.getExpiresAt());
        } catch (IllegalArgumentException e) {
            throw new AccessTokenValidateException("accessToken", e.getMessage());
        }
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }
    }

    private void validateClientUid(UUID clientUID) {
        if (clientUID == null) {
            throw new IllegalArgumentException("Client UID cannot be null");
        }
    }

    private void validateCreatedAt(Date createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Creation date cannot be null");
        }

        Date now = new Date();
        if (createdAt.after(now)) {
            throw new IllegalArgumentException("Creation date cannot be in the future");
        }
    }

    private void validateExpiresAt(Date expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }

        Date now = new Date();
        if (expiresAt.before(now)) {
            throw new IllegalArgumentException("Token has already expired");
        }
    }

}
