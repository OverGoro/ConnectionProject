package com.service.auth.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;

public class AuthObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID REFRESH_TOKEN_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final String VALID_EMAIL = "test@example.com";
    public static final String VALID_PASSWORD = "securePassword123";
    public static final String VALID_USERNAME = "testuser";
    public static final Date BIRTH_DATE = new Date(System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000); // 25 years ago

    public static ClientBLM createValidClientBLM() {
        return new ClientBLM(
            CLIENT_UUID,
            BIRTH_DATE,
            VALID_EMAIL,
            VALID_PASSWORD,
            VALID_USERNAME
        );
    }

    public static ClientDALM createValidClientDALM() {
        return new ClientDALM(
            CLIENT_UUID,
            BIRTH_DATE,
            VALID_EMAIL,
            VALID_PASSWORD,
            VALID_USERNAME
        );
    }

    public static AccessTokenBLM createValidAccessTokenBLM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 600000); // 10 minutes
        String tokenString = "valid.access.token.string";
        
        return new AccessTokenBLM(
            tokenString,
            CLIENT_UUID,
            createdAt,
            expiresAt
        );
    }

    public static RefreshTokenBLM createValidRefreshTokenBLM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 86400000); // 24 hours
        String tokenString = "valid.refresh.token.string";
        
        return new RefreshTokenBLM(
            tokenString,
            REFRESH_TOKEN_UUID,
            CLIENT_UUID,
            createdAt,
            expiresAt
        );
    }

    public static RefreshTokenBLM createExpiredRefreshTokenBLM() {
        Date createdAt = new Date(System.currentTimeMillis() - 86400000); // 24 hours ago
        Date expiresAt = new Date(createdAt.getTime() + 3600000); // 1 hour, already expired
        String tokenString = "expired.refresh.token.string";
        
        return new RefreshTokenBLM(
            tokenString,
            REFRESH_TOKEN_UUID,
            CLIENT_UUID,
            createdAt,
            expiresAt
        );
    }
}