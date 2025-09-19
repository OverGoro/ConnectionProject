package com.connection.scheme.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Exception Tests")
class ConnectionSchemeExceptionTest {

    @Test
    @DisplayName("BaseConnectionSchemeException toString format")
    void testBaseConnectionSchemeExceptionToString() {
        String schemeUid = "test-scheme-123";
        BaseConnectionSchemeException exception = new BaseConnectionSchemeException(schemeUid);
        String result = exception.toString();
        assertThat(result).contains(schemeUid);
    }

    @Test
    @DisplayName("ConnectionSchemeValidateException toString format")
    void testConnectionSchemeValidateExceptionToString() {
        String schemeUid = "test-scheme-456";
        String description = "Validation failed";
        ConnectionSchemeValidateException exception = new ConnectionSchemeValidateException(schemeUid, description);
        String result = exception.toString();
        assertThat(result).contains(schemeUid);
        assertThat(result).contains(description);
    }

    @Test
    @DisplayName("ConnectionSchemeNotFoundException toString format")
    void testConnectionSchemeNotFoundExceptionToString() {
        String schemeUid = "test-scheme-789";
        ConnectionSchemeNotFoundException exception = new ConnectionSchemeNotFoundException(schemeUid);
        String result = exception.toString();
        assertThat(result).contains(schemeUid);
    }

    @Test
    @DisplayName("ConnectionSchemeAlreadyExistsException toString format")
    void testConnectionSchemeAlreadyExistsExceptionToString() {
        String schemeUid = "test-scheme-012";
        ConnectionSchemeAlreadyExistsException exception = new ConnectionSchemeAlreadyExistsException(schemeUid);
        String result = exception.toString();
        assertThat(result).contains(schemeUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        ConnectionSchemeValidateException validateException = new ConnectionSchemeValidateException("uid", "desc");
        ConnectionSchemeAlreadyExistsException existsException = new ConnectionSchemeAlreadyExistsException("uid");
        ConnectionSchemeNotFoundException notFoundException = new ConnectionSchemeNotFoundException("uid");

        assertThat(validateException).isInstanceOf(BaseConnectionSchemeException.class);
        assertThat(existsException).isInstanceOf(BaseConnectionSchemeException.class);
        assertThat(notFoundException).isInstanceOf(BaseConnectionSchemeException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}