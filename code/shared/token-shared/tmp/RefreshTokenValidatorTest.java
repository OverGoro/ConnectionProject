package com.connection.token.validator;

import static com.connection.token.mother.TokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.token.exception.RefreshTokenValidateException;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.model.RefreshTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Refresh Token Validator Tests")
class RefreshTokenValidatorTest {

    private RefreshTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RefreshTokenValidator();
    }

    @Test
    @DisplayName("Validate valid RefreshTokenDTO - Positive")
    void testValidateRefreshTokenDTO_Positive() {
        RefreshTokenDTO token = createValidRefreshTokenDTO();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid RefreshTokenBLM - Positive")
    void testValidateRefreshTokenBLM_Positive() {
        RefreshTokenBLM token = createValidRefreshTokenBLM();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid RefreshTokenDALM - Positive")
    void testValidateRefreshTokenDALM_Positive() {
        RefreshTokenDALM token = createValidRefreshTokenDALM();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate null RefreshTokenDTO - Negative")
    void testValidateNullRefreshTokenDTO_Negative() {
        RefreshTokenDTO token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDTO with empty token - Negative")
    void testValidateRefreshTokenDTOWithEmptyToken_Negative() {
        RefreshTokenDTO token = createRefreshTokenDTOWithEmptyToken();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null RefreshTokenBLM - Negative")
    void testValidateNullRefreshTokenBLM_Negative() {
        RefreshTokenBLM token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenBLM with null fields - Negative")
    void testValidateRefreshTokenBLMWithNullFields_Negative() {
        RefreshTokenBLM token = createRefreshTokenBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null RefreshTokenDALM - Negative")
    void testValidateNullRefreshTokenDALM_Negative() {
        RefreshTokenDALM token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDALM with null fields - Negative")
    void testValidateRefreshTokenDALMWithNullFields_Negative() {
        RefreshTokenDALM token = createRefreshTokenDALMWithNullFields();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenBLM with expired token - Negative")
    void testValidateRefreshTokenBLMWithExpiredToken_Negative() {
        RefreshTokenBLM token = createRefreshTokenBLMWithExpiredToken();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDALM with future created date - Negative")
    void testValidateRefreshTokenDALMWithFutureCreatedAt_Negative() {
        RefreshTokenDALM token = createRefreshTokenDALMWithFutureCreatedAt();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }
}