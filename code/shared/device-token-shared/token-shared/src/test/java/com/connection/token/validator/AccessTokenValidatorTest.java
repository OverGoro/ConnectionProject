package com.connection.token.validator;

import static com.connection.token.mother.TokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.token.exception.AccessTokenValidateException;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDALM;
import com.connection.token.model.AccessTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Access Token Validator Tests")
class AccessTokenValidatorTest {

    private AccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccessTokenValidator();
    }

    @Test
    @DisplayName("Validate valid AccessTokenDTO - Positive")
    void testValidateAccessTokenDTO_Positive() {
        AccessTokenDTO token = createValidAccessTokenDTO();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid AccessTokenBLM - Positive")
    void testValidateAccessTokenBLM_Positive() {
        AccessTokenBLM token = createValidAccessTokenBLM();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid AccessTokenDALM - Positive")
    void testValidateAccessTokenDALM_Positive() {
        AccessTokenDALM token = createValidAccessTokenDALM();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate null AccessTokenDTO - Negative")
    void testValidateNullAccessTokenDTO_Negative() {
        AccessTokenDTO token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDTO with empty token - Negative")
    void testValidateAccessTokenDTOWithEmptyToken_Negative() {
        AccessTokenDTO token = new AccessTokenDTO("");
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null AccessTokenBLM - Negative")
    void testValidateNullAccessTokenBLM_Negative() {
        AccessTokenBLM token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenBLM with expired token - Negative")
    void testValidateAccessTokenBLMWithExpiredToken_Negative() {
        Date expiredDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        AccessTokenBLM token = new AccessTokenBLM(
            "test-token",
            createValidAccessTokenBLM().getClientUID(),
            createValidAccessTokenBLM().getCreatedAt(),
            expiredDate
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenBLM with future created date - Negative")
    void testValidateAccessTokenBLMWithFutureCreatedAt_Negative() {
        Date futureDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60);
        AccessTokenBLM token = new AccessTokenBLM(
            "test-token",
            createValidAccessTokenBLM().getClientUID(),
            futureDate,
            createValidAccessTokenBLM().getExpiresAt()
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null AccessTokenDALM - Negative")
    void testValidateNullAccessTokenDALM_Negative() {
        AccessTokenDALM token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDALM with null client UID - Negative")
    void testValidateAccessTokenDALMWithNullClientUID_Negative() {
        AccessTokenDALM token = new AccessTokenDALM(
            null,
            createValidAccessTokenDALM().getCreatedAt(),
            createValidAccessTokenDALM().getExpiresAt()
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDALM with expired token - Negative")
    void testValidateAccessTokenDALMWithExpiredToken_Negative() {
        Date expiredDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        AccessTokenDALM token = new AccessTokenDALM(
            createValidAccessTokenDALM().getClientUID(),
            createValidAccessTokenDALM().getCreatedAt(),
            expiredDate
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }
}