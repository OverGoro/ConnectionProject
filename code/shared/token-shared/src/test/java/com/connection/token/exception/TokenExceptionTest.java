package com.connection.token.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Token Exception Tests")
class TokenExceptionTest {

    @Test
    @DisplayName("BaseTokenException toString format")
    void testBaseTokenExceptionToString() {
        String tokenUid = "test-token-123";
        BaseTokenException exception = new BaseTokenException(tokenUid);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
    }

    @Test
    @DisplayName("RefreshTokenValidateException toString format")
    void testRefreshTokenValidateExceptionToString() {
        String tokenUid = "test-token-456";
        String description = "Validation failed";
        RefreshTokenValidateException exception = new RefreshTokenValidateException(tokenUid, description);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
        assertThat(result).contains(description);
    }

    @Test
    @DisplayName("AccessTokenValidateException toString format")
    void testAccessTokenValidateExceptionToString() {
        String tokenUid = "test-token-789";
        String description = "Validation failed";
        AccessTokenValidateException exception = new AccessTokenValidateException(tokenUid, description);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
        assertThat(result).contains(description);
    }

    @Test
    @DisplayName("RefreshTokenAlreadyExistsException toString format")
    void testRefreshTokenAlreadyExistsExceptionToString() {
        String tokenUid = "test-token-012";
        RefreshTokenAlreadyExisistsException exception = new RefreshTokenAlreadyExisistsException(tokenUid);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
    }

    @Test
    @DisplayName("RefreshTokenNotFoundException toString format")
    void testRefreshTokenNotFoundExceptionToString() {
        String tokenUid = "test-token-345";
        RefreshTokenNotFoundException exception = new RefreshTokenNotFoundException(tokenUid);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
    }

    @Test
    @DisplayName("RefreshTokenExpiredException toString format")
    void testRefreshTokenExpiredExceptionToString() {
        String tokenUid = "test-token-678";
        RefreshTokenExpiredException exception = new RefreshTokenExpiredException(tokenUid);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
    }

    @Test
    @DisplayName("RefreshTokenAddException toString format")
    void testRefreshTokenAddExceptionToString() {
        String tokenUid = "test-token-901";
        RefreshTokenAddException exception = new RefreshTokenAddException(tokenUid);
        String result = exception.toString();
        assertThat(result).contains(tokenUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        RefreshTokenValidateException validateException = new RefreshTokenValidateException("uid", "desc");
        RefreshTokenAlreadyExisistsException existsException = new RefreshTokenAlreadyExisistsException("uid");
        RefreshTokenNotFoundException notFoundException = new RefreshTokenNotFoundException("uid");
        RefreshTokenExpiredException expiredException = new RefreshTokenExpiredException("uid");
        RefreshTokenAddException addException = new RefreshTokenAddException("uid");
        AccessTokenValidateException accessValidateException = new AccessTokenValidateException("uid", "desc");

        assertThat(validateException).isInstanceOf(BaseTokenException.class);
        assertThat(existsException).isInstanceOf(BaseTokenException.class);
        assertThat(notFoundException).isInstanceOf(BaseTokenException.class);
        assertThat(expiredException).isInstanceOf(BaseTokenException.class);
        assertThat(addException).isInstanceOf(BaseTokenException.class);
        assertThat(accessValidateException).isInstanceOf(BaseTokenException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}