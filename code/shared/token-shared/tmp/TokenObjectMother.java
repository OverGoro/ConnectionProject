package com.connection.token.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDALM;
import com.connection.token.model.AccessTokenDTO;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.model.RefreshTokenDTO;

public class TokenObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final Date DEFAULT_CREATED_AT = new Date(System.currentTimeMillis() - 1000L * 60 * 5);
    private static final Date DEFAULT_EXPIRES_AT = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24);

    public static RefreshTokenDTO createValidRefreshTokenDTO() {
        return new RefreshTokenDTO(DEFAULT_TOKEN);
    }

    public static RefreshTokenBLM createValidRefreshTokenBLM() {
        return new RefreshTokenBLM(DEFAULT_TOKEN, DEFAULT_UID, DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    public static RefreshTokenDALM createValidRefreshTokenDALM() {
        return new RefreshTokenDALM(DEFAULT_TOKEN, DEFAULT_UID, DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    public static AccessTokenDTO createValidAccessTokenDTO() {
        return new AccessTokenDTO(DEFAULT_TOKEN);
    }

    public static AccessTokenBLM createValidAccessTokenBLM() {
        return new AccessTokenBLM(DEFAULT_TOKEN, DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    public static AccessTokenDALM createValidAccessTokenDALM() {
        return new AccessTokenDALM(DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    public static RefreshTokenDTO createRefreshTokenDTOWithEmptyToken() {
        return new RefreshTokenDTO("");
    }

    public static RefreshTokenBLM createRefreshTokenBLMWithNullFields() {
        return new RefreshTokenBLM(null, null, null, null, null);
    }

    public static RefreshTokenDALM createRefreshTokenDALMWithNullFields() {
        return new RefreshTokenDALM(null, null, null, null, null);
    }

    public static RefreshTokenBLM createRefreshTokenBLMWithExpiredToken() {
        Date expiredDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        return new RefreshTokenBLM(DEFAULT_TOKEN, DEFAULT_UID, DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, expiredDate);
    }

    public static RefreshTokenDALM createRefreshTokenDALMWithFutureCreatedAt() {
        Date futureDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60);
        return new RefreshTokenDALM(DEFAULT_TOKEN, DEFAULT_UID, DEFAULT_CLIENT_UID, futureDate, DEFAULT_EXPIRES_AT);
    }

    public static RefreshTokenDALM createRefreshTokenForClient(UUID clientUid) {
        return new RefreshTokenDALM(DEFAULT_TOKEN, UUID.randomUUID(), clientUid, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }
}