package com.connection.token.validator;

import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenBlm;
import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenDalm;
import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.connection.token.exception.AccessTokenValidateException;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import com.connection.token.model.AccessTokenDto;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Access Token Validator Tests")
class AccessTokenValidatorTest {

    private AccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccessTokenValidator();
    }

    @Test
    @DisplayName("Validate valid AccessTokenDto - Positive")
    void testValidateAccessTokenDto_Positive() {
        AccessTokenDto token = createValidAccessTokenDto();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid AccessTokenBlm - Positive")
    void testValidateAccessTokenBlm_Positive() {
        AccessTokenBlm token = createValidAccessTokenBlm();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate valid AccessTokenDalm - Positive")
    void testValidateAccessTokenDalm_Positive() {
        AccessTokenDalm token = createValidAccessTokenDalm();
        assertThat(token).isNotNull();
        validator.validate(token);
    }

    @Test
    @DisplayName("Validate null AccessTokenDto - Negative")
    void testValidateNullAccessTokenDto_Negative() {
        AccessTokenDto token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDto with empty token - Negative")
    void testValidateAccessTokenDtoWithEmptyToken_Negative() {
        AccessTokenDto token = new AccessTokenDto("");
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null AccessTokenBlm - Negative")
    void testValidateNullAccessTokenBlm_Negative() {
        AccessTokenBlm token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenBlm with expired token - Negative")
    void testValidateAccessTokenBlmWithExpiredToken_Negative() {
        Date expiredDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        AccessTokenBlm token = new AccessTokenBlm(
            "test-token",
            createValidAccessTokenBlm().getClientUid(),
            createValidAccessTokenBlm().getCreatedAt(),
            expiredDate
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenBlm with future created date - Negative")
    void testValidateAccessTokenBlmWithFutureCreatedAt_Negative() {
        Date futureDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60);
        AccessTokenBlm token = new AccessTokenBlm(
            "test-token",
            createValidAccessTokenBlm().getClientUid(),
            futureDate,
            createValidAccessTokenBlm().getExpiresAt()
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate null AccessTokenDalm - Negative")
    void testValidateNullAccessTokenDalm_Negative() {
        AccessTokenDalm token = null;
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDalm with null client UID - Negative")
    void testValidateAccessTokenDalmWithNullClientUid_Negative() {
        AccessTokenDalm token = new AccessTokenDalm(
            null,
            createValidAccessTokenDalm().getCreatedAt(),
            createValidAccessTokenDalm().getExpiresAt()
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate AccessTokenDalm with expired token - Negative")
    void testValidateAccessTokenDalmWithExpiredToken_Negative() {
        Date expiredDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60);
        AccessTokenDalm token = new AccessTokenDalm(
            createValidAccessTokenDalm().getClientUid(),
            createValidAccessTokenDalm().getCreatedAt(),
            expiredDate
        );
        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(AccessTokenValidateException.class);
    }
}