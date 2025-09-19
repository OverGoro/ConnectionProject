package com.connection.client.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Exception Tests")
class ClientExceptionTest {

    @Test
    @DisplayName("BaseClientException toString format")
    void testBaseClientExceptionToString() {
        String clientUid = "test-client-123";
        BaseClientException exception = new BaseClientException(clientUid);
        String result = exception.toString();
        assertThat(result).contains(clientUid);
    }

    @Test
    @DisplayName("ClientValidateException toString format")
    void testClientValidateExceptionToString() {
        String clientUid = "test-client-456";
        String description = "Validation failed";
        ClientValidateException exception = new ClientValidateException(clientUid, description);
        String result = exception.toString();
        assertThat(result).contains(clientUid);
        assertThat(result).contains(description);
    }

    @Test
    @DisplayName("ClientAlreadyExistsException toString format")
    void testClientAlreadyExistsExceptionToString() {
        String clientUid = "test-client-789";
        ClientAlreadyExisistsException exception = new ClientAlreadyExisistsException(clientUid);
        String result = exception.toString();
        assertThat(result).contains(clientUid);
    }

    @Test
    @DisplayName("ClientNotFoundException toString format")
    void testClientNotFoundExceptionToString() {
        String clientUid = "test-client-012";
        ClientNotFoundException exception = new ClientNotFoundException(clientUid);
        String result = exception.toString();
        assertThat(result).contains(clientUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        ClientValidateException validateException = new ClientValidateException("uid", "desc");
        ClientAlreadyExisistsException existsException = new ClientAlreadyExisistsException("uid");
        ClientNotFoundException notFoundException = new ClientNotFoundException("uid");

        assertThat(validateException).isInstanceOf(BaseClientException.class);
        assertThat(existsException).isInstanceOf(BaseClientException.class);
        assertThat(notFoundException).isInstanceOf(BaseClientException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}