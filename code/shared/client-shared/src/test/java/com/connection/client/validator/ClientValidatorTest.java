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
import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.model.ClientDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Validator Tests")
class ClientValidatorTest {

    private ClientValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClientValidator();
    }

    @Test
    @DisplayName("Validate valid ClientDto - Positive")
    void testValidateClientDto_Positive() {
        ClientDto client = createValidClientDto();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate valid ClientBlm - Positive")
    void testValidateClientBlm_Positive() {
        ClientBlm client = createValidClientBlm();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate valid ClientDalm - Positive")
    void testValidateClientDalm_Positive() {
        ClientDalm client = createValidClientDalm();
        assertThat(client).isNotNull();
        validator.validate(client);
    }

    @Test
    @DisplayName("Validate null ClientDto - Negative")
    void testValidateNullClientDto_Negative() {
        ClientDto client = null;
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    // @Test
    // @DisplayName("Validate ClientDto with invalid email - Negative")
    // void testValidateClientDtoWithInvalidEmail_Negative() {
    //     ClientDto client = createClientDtoWithInvalidEmail();
    //     assertThatThrownBy(() -> validator.validate(client))
    //             .isInstanceOf(ClientValidateException.class);
    // }

    @Test
    @DisplayName("Validate ClientDto with short password - Negative")
    void testValidateClientDtoWithShortPassword_Negative() {
        ClientDto client = createClientDtoWithShortPassword();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDto with invalid username - Negative")
    void testValidateClientDtoWithInvalidUsername_Negative() {
        ClientDto client = createClientDtoWithInvalidUsername();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDto with future birth date - Negative")
    void testValidateClientDtoWithFutureBirthDate_Negative() {
        ClientDto client = createClientDtoWithFutureBirthDate();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate null ClientBlm - Negative")
    void testValidateNullClientBlm_Negative() {
        ClientBlm client = null;
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientBlm with null fields - Negative")
    void testValidateClientBlmWithNullFields_Negative() {
        ClientBlm client = createClientBlmWithNullFields();
        assertThatThrownBy(() -> validator.validate(client))
                .isInstanceOf(ClientValidateException.class);
    }

    @Test
    @DisplayName("Validate ClientDto with empty email - Negative")
    void testValidateClientDtoWithEmptyEmail_Negative() {
        ClientDto client = ClientDto.builder()
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
    @DisplayName("Validate ClientDto with long email - Negative")
    void testValidateClientDtoWithLongEmail_Negative() {
        String longEmail = "a".repeat(256) + "@example.com";
        ClientDto client = ClientDto.builder()
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