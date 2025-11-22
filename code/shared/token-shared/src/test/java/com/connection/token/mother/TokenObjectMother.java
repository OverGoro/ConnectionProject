package com.connection.token.mother;

import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import com.connection.token.model.AccessTokenDto;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
import com.connection.token.model.RefreshTokenDto;
import java.util.Date;
import java.util.UUID;

/** . */
public class TokenObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NT"
                    + "Y3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIy"
                    + "fQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final Date DEFAULT_CREATED_AT =
            new Date(System.currentTimeMillis() - 1000L * 60 * 5);
    private static final Date DEFAULT_EXPIRES_AT =
            new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24);

    /** . */
    public static RefreshTokenDto createValidRefreshTokenDto() {
        return new RefreshTokenDto(DEFAULT_TOKEN);
    }

    /** . */
    public static RefreshTokenBlm createValidRefreshTokenBlm() {
        return new RefreshTokenBlm(DEFAULT_TOKEN, DEFAULT_UID,
                DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    /** . */
    public static RefreshTokenDalm createValidRefreshTokenDalm() {
        return new RefreshTokenDalm(DEFAULT_TOKEN, DEFAULT_UID,
                DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    /** . */
    public static AccessTokenDto createValidAccessTokenDto() {
        return new AccessTokenDto(DEFAULT_TOKEN);
    }

    /** . */
    public static AccessTokenBlm createValidAccessTokenBlm() {
        return new AccessTokenBlm(DEFAULT_TOKEN, DEFAULT_CLIENT_UID,
                DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }

    /** . */
    public static AccessTokenDalm createValidAccessTokenDalm() {
        return new AccessTokenDalm(DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT,
                DEFAULT_EXPIRES_AT);
    }

    /** . */
    public static RefreshTokenDto createRefreshTokenDtoWithEmptyToken() {
        return new RefreshTokenDto("");
    }

    /** . */
    public static RefreshTokenBlm createRefreshTokenBlmWithNullFields() {
        return new RefreshTokenBlm(null, null, null, null, null);
    }

    /** . */
    public static RefreshTokenDalm createRefreshTokenDalmWithNullFields() {
        return new RefreshTokenDalm(null, null, null, null, null);
    }

    /** . */
    public static RefreshTokenBlm createRefreshTokenBlmWithExpiredToken() {
        Date expiredDate =
                new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        return new RefreshTokenBlm(DEFAULT_TOKEN, DEFAULT_UID,
                DEFAULT_CLIENT_UID, DEFAULT_CREATED_AT, expiredDate);
    }

    /** . */
    public static RefreshTokenDalm createRefreshTokenDalmWithFutureCreatedAt() {
        Date futureDate =
                new Date(System.currentTimeMillis() + 1000L * 60 * 60);
        return new RefreshTokenDalm(DEFAULT_TOKEN, DEFAULT_UID,
                DEFAULT_CLIENT_UID, futureDate, DEFAULT_EXPIRES_AT);
    }

    /** . */
    public static RefreshTokenDalm createRefreshTokenForClient(UUID clientUid) {
        return new RefreshTokenDalm(DEFAULT_TOKEN, UUID.randomUUID(), clientUid,
                DEFAULT_CREATED_AT, DEFAULT_EXPIRES_AT);
    }
}
