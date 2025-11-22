package com.connection.service.auth.mother;

import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.model.ClientDto;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.RefreshTokenBlm;
import java.util.Date;
import java.util.UUID;


/** . */
public class AuthObjectMother {

    public static final UUID CLIENT_UUID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID REFRESH_TOKEN_UUID =
            UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final String VALID_EMAIL = "test@example.com";
    public static final String VALID_PASSWORD = "securePassword123";
    public static final String VALID_USERNAME = "testuser";
    public static final Date BIRTH_DATE = new Date(
            System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000); // 25 years ago

    /** . */
    public static ClientDto randomValidClientDto() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String threadId = String.valueOf(Thread.currentThread().getId());
        String uniqueSuffix = timestamp + "_" + threadId;

        return new ClientDto(UUID.randomUUID(),
                new Date(System.currentTimeMillis()
                        - 25L * 365 * 24 * 60 * 60 * 1000),
                "test_" + uniqueSuffix + "@example.com",
                "Password123!" + uniqueSuffix, "user_" + uniqueSuffix);
    }

    /** . */
    public static ClientBlm randomValidClientBlm() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String threadId = String.valueOf(Thread.currentThread().getId());
        String uniqueSuffix = timestamp + "_" + threadId;

        return new ClientBlm(UUID.randomUUID(),
                new Date(System.currentTimeMillis()
                        - 25L * 365 * 24 * 60 * 60 * 1000),
                "test_" + uniqueSuffix + "@example.com",
                "Password123!" + uniqueSuffix, "user_" + uniqueSuffix);
    }

    /** . */
    public static ClientBlm createValidClientBlm() {
        return new ClientBlm(CLIENT_UUID, BIRTH_DATE, VALID_EMAIL,
                VALID_PASSWORD, VALID_USERNAME);
    }

    /** . */
    public static ClientDalm createValidClientDalm() {
        return new ClientDalm(CLIENT_UUID, BIRTH_DATE, VALID_EMAIL,
                VALID_PASSWORD, VALID_USERNAME);
    }

    /** . */
    public static AccessTokenBlm createValidAccessTokenBlm() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 600000); // 10 minutes
        String tokenString = "valid.access.token.string";

        return new AccessTokenBlm(tokenString, CLIENT_UUID, createdAt,
                expiresAt);
    }

    /** . */
    public static RefreshTokenBlm createValidRefreshTokenBlm() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 86400000); // 24 hours
        String tokenString = "valid.refresh.token.string";

        return new RefreshTokenBlm(tokenString, REFRESH_TOKEN_UUID, CLIENT_UUID,
                createdAt, expiresAt);
    }

    /** . */
    public static RefreshTokenBlm createExpiredRefreshTokenBlm() {
        Date createdAt = new Date(System.currentTimeMillis() - 86400000); // 24 hours ago
        Date expiresAt = new Date(createdAt.getTime() + 3600000); // 1 hour, already expired
        String tokenString = "expired.refresh.token.string";

        return new RefreshTokenBlm(tokenString, REFRESH_TOKEN_UUID, CLIENT_UUID,
                createdAt, expiresAt);
    }
}
