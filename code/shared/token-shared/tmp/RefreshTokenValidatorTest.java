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
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
import com.connection.token.model.RefreshTokenDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Refresh Token Validator Tests")
class RefreshTokenValidatorTest {

    private RefreshTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RefreshTokenValidator();
    }

    @Test
    @DisplayName("Validate valid RefreshTokenDto - Positive")
    void testValidateRefreshTokenDto_Positive() {
        RefreshTokenDto token = createValidRefreshTokenDto();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid RefreshTokenBlm - Positive")
    void testValidateRefreshTokenBlm_Positive() {
        RefreshTokenBlm token = createValidRefreshTokenBlm();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid RefreshTokenDalm - Positive")
    void testValidateRefreshTokenDalm_Positive() {
        RefreshTokenDalm token = createValidRefreshTokenDalm();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate null RefreshTokenDto - Negative")
    void testValidateNullRefreshTokenDto_Negative() {
        RefreshTokenDto token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDto with empty token - Negative")
    void testValidateRefreshTokenDtoWithEmptyToken_Negative() {
        RefreshTokenDto token = createRefreshTokenDtoWithEmptyToken();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null RefreshTokenBlm - Negative")
    void testValidateNullRefreshTokenBlm_Negative() {
        RefreshTokenBlm token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenBlm with null fields - Negative")
    void testValidateRefreshTokenBlmWithNullFields_Negative() {
        RefreshTokenBlm token = createRefreshTokenBlmWithNullFields();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null RefreshTokenDalm - Negative")
    void testValidateNullRefreshTokenDalm_Negative() {
        RefreshTokenDalm token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDalm with null fields - Negative")
    void testValidateRefreshTokenDalmWithNullFields_Negative() {
        RefreshTokenDalm token = createRefreshTokenDalmWithNullFields();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenBlm with expired token - Negative")
    void testValidateRefreshTokenBlmWithExpiredToken_Negative() {
        RefreshTokenBlm token = createRefreshTokenBlmWithExpiredToken();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate RefreshTokenDalm with future created date - Negative")
    void testValidateRefreshTokenDalmWithFutureCreatedAt_Negative() {
        RefreshTokenDalm token = createRefreshTokenDalmWithFutureCreatedAt();
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(RefreshTokenValidateException.class);
    }
}