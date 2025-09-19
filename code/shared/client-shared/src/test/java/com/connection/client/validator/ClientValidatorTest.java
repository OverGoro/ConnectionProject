package com.connection.client.validator;

import static com.connection.client.mother.ClientObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.client.exception.ClientValidateException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.model.ClientDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Validator Tests")
class ClientValidatorTest {

    private ClientValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClientValidator();
    }

    @Test
    @DisplayName("Validate valid ClientDTO - Positive")
    void testValidateClientDTO_Positive() {
        ClientDTO client = createValidClientDTO();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate valid ClientBLM - Positive")
    void testValidateClientBLM_Positive() {
        ClientBLM client = createValidClientBLM();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate valid ClientDALM - Positive")
    void testValidateClientDALM_Positive() {
        ClientDALM client = createValidClientDALM();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate null ClientDTO - Negative")
    void testValidateNullClientDTO_Negative() {
        ClientDTO client = null;
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with invalid email - Negative")
    void testValidateClientDTOWithInvalidEmail_Negative() {
        ClientDTO client = createClientDTOWithInvalidEmail();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with short password - Negative")
    void testValidateClientDTOWithShortPassword_Negative() {
        ClientDTO client = createClientDTOWithShortPassword();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with invalid username - Negative")
    void testValidateClientDTOWithInvalidUsername_Negative() {
        ClientDTO client = createClientDTOWithInvalidUsername();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with future birth date - Negative")
    void testValidateClientDTOWithFutureBirthDate_Negative() {
        ClientDTO client = createClientDTOWithFutureBirthDate();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate null ClientBLM - Negative")
    void testValidateNullClientBLM_Negative() {
        ClientBLM client = null;
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientBLM with null fields - Negative")
    void testValidateClientBLMWithNullFields_Negative() {
        ClientBLM client = createClientBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with empty email - Negative")
    void testValidateClientDTOWithEmptyEmail_Negative() {
        ClientDTO client = ClientDTO.builder()
                .uid(UUID.randomUUID())
                .birthDate(new Date())
                .email("")
                .password("Password123")
                .username("testuser")
                .build();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDTO with long email - Negative")
    void testValidateClientDTOWithLongEmail_Negative() {
        String longEmail = "a".repeat(256) + "@example.com";
        ClientDTO client = ClientDTO.builder()
                .uid(UUID.randomUUID())
                .birthDate(new Date())
                .email(longEmail)
                .password("Password123")
                .username("testuser")
                .build();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }
}