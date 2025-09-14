package com.connection.token.validator;

import java.util.Date;
import java.util.UUID;

import com.connection.token.exception.RefreshTokenValidateException;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.model.RefreshTokenDTO;

public class RefreshTokenValidator {
    public void validate(RefreshTokenDTO refreshToken) {
        if (refreshToken == null) {
            throw new RefreshTokenValidateException("null", "Refresh token is null");
        }
        try {
            validateToken(refreshToken.getToken());
        } catch (IllegalArgumentException e) {
            throw new RefreshTokenValidateException("refreshToken", e.getMessage());
        }
    }

    public void validate(RefreshTokenBLM refreshToken) {
        if (refreshToken == null) {
            throw new RefreshTokenValidateException("null", "Refresh token is null");
        }
        try {
            validateToken(refreshToken.getToken());
            validateUID(refreshToken.getUid());
            validateClientUID(refreshToken.getClientUID());
            validateCreatedAt(refreshToken.getCreatedAt());
            validateExpiresAt(refreshToken.getExpiresAt());
        } catch (IllegalArgumentException e) {
            if (refreshToken.getUid() != null)
                throw new RefreshTokenValidateException(refreshToken.getUid().toString(), e.getMessage());
            else
                throw new RefreshTokenValidateException("null", e.getMessage());
        }
    }

    public void validate(RefreshTokenDALM refreshToken) {
        if (refreshToken == null) {
            throw new RefreshTokenValidateException("null", "Refresh token is null");
        }
        try {
            validateUID(refreshToken.getUid());
            validateClientUID(refreshToken.getClientUID());
            validateCreatedAt(refreshToken.getCreatedAt());
            validateExpiresAt(refreshToken.getExpiresAt());
        } catch (IllegalArgumentException e) {
            if (refreshToken.getUid() != null)
                throw new RefreshTokenValidateException(refreshToken.getUid().toString(), e.getMessage());
            else
                throw new RefreshTokenValidateException("null", e.getMessage());
        }
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }
    }

    private void validateUID(UUID UID) {
        if (UID == null) {
            throw new IllegalArgumentException("UID cannot be null");
        }
    }

    private void validateClientUID(UUID clientUID) {
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
