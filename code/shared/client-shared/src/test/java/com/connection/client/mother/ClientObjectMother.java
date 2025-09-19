package com.connection.client.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.model.ClientDTO;

public class ClientObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final Date DEFAULT_BIRTH_DATE = new Date(
            System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000);
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PASSWORD = "Password123";
    private static final String DEFAULT_USERNAME = "testuser";

    public static ClientDTO createValidClientDTO() {
        return ClientDTO.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientBLM createValidClientBLM() {
        return ClientBLM.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientDALM createValidClientDALM() {
        return ClientDALM.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientDTO createClientDTOWithInvalidEmail() {
        return ClientDTO.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email("invalid-email")
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientDTO createClientDTOWithShortPassword() {
        return ClientDTO.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(DEFAULT_EMAIL)
                .password("short")
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientDTO createClientDTOWithInvalidUsername() {
        return ClientDTO.builder()
                .uid(DEFAULT_UID)
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .username("ab")
                .build();
    }

    public static ClientDTO createClientDTOWithFutureBirthDate() {
        return ClientDTO.builder()
                .uid(DEFAULT_UID)
                .birthDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static ClientBLM createClientBLMWithNullFields() {
        return ClientBLM.builder()
                .uid(null)
                .birthDate(null)
                .email(null)
                .password(null)
                .username(null)
                .build();
    }

    public static ClientDALM createClientForEmail(String email) {
        return ClientDALM.builder()
                .uid(UUID.randomUUID())
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(email)
                .password(DEFAULT_PASSWORD)
                .username("user_" + email.split("@")[0])
                .build();
    }

    public static ClientDALM createClientForUsername(String username) {
        return ClientDALM.builder()
                .uid(UUID.randomUUID())
                .birthDate(DEFAULT_BIRTH_DATE)
                .email(username + "@example.com")
                .password(DEFAULT_PASSWORD)
                .username(username)
                .build();
    }
}