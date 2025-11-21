package com.connection.client.mother;

import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.model.ClientDto;
import java.util.Date;
import java.util.UUID;

/** . */
public class ClientObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final Date DEFAULT_BIRTH_DATE = new Date(
            System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000);
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PASSWORD = "Password123";
    private static final String DEFAULT_USERNAME = "testuser";

    /** . */
    public static ClientDto createValidClientDto() {
        return ClientDto.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD).username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientBlm createValidClientBlm() {
        return ClientBlm.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD).username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientDalm createValidClientDalm() {
        return ClientDalm.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD).username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientDto createClientDtoWithInvalidEmail() {
        return ClientDto.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email("invalid-email")
                .password(DEFAULT_PASSWORD).username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientDto createClientDtoWithShortPassword() {
        return ClientDto.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email(DEFAULT_EMAIL)
                .password("short").username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientDto createClientDtoWithInvalidUsername() {
        return ClientDto.builder().uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE).email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD).username("ab").build();
    }

    /** . */
    public static ClientDto createClientDtoWithFutureBirthDate() {
        return ClientDto.builder().uid(DEFAULT_UID)
                .birthDate(new Date(System.currentTimeMillis()
                        + 365L * 24 * 60 * 60 * 1000))
                .email(DEFAULT_EMAIL).password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME).build();
    }

    /** . */
    public static ClientBlm createClientBlmWithNullFields() {
        return ClientBlm.builder().uid(null).birthDate(null).email(null)
                .password(null).username(null).build();
    }

    /** . */
    public static ClientDalm createClientForEmail(String email) {
        return ClientDalm.builder().uid(UUID.randomUUID())
                .birthDate(DEFAULT_BIRTH_DATE).email(email)
                .password(DEFAULT_PASSWORD)
                .username("user_" + email.split("@")[0]).build();
    }

    /** . */
    public static ClientDalm createClientForUsername(String username) {
        return ClientDalm.builder().uid(UUID.randomUUID())
                .birthDate(DEFAULT_BIRTH_DATE).email(username + "@example.com")
                .password(DEFAULT_PASSWORD).username(username).build();
    }
}
