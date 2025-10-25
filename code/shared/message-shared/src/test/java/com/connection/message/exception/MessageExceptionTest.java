package com.connection.message.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Exception Tests")
class MessageExceptionTest {

    @Test
    @DisplayName("BaseMessageException toString format")
    void testBaseMessageExceptionToString() {
        String messageUid = "test-message-123";
        BaseMessageException exception = new BaseMessageException(messageUid);
        String result = exception.toString();
        assertThat(result).contains(messageUid);
    }

    @Test
    @DisplayName("MessageValidateException toString format")
    void testMessageValidateExceptionToString() {
        String messageUid = "test-message-456";
        String description = "Validation failed";
        MessageValidateException exception = new MessageValidateException(messageUid, description);
        String result = exception.toString();
        assertThat(result).contains(messageUid);
        assertThat(result).contains(description);
    }

    @Test
    @DisplayName("MessageAddException toString format")
    void testMessageAddExceptionToString() {
        String messageUid = "test-message-789";
        MessageAddException exception = new MessageAddException(messageUid);
        String result = exception.toString();
        assertThat(result).contains(messageUid);
    }

    @Test
    @DisplayName("MessageNotFoundException toString format")
    void testMessageNotFoundExceptionToString() {
        String messageUid = "test-message-012";
        MessageNotFoundException exception = new MessageNotFoundException(messageUid);
        String result = exception.toString();
        assertThat(result).contains(messageUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        MessageValidateException validateException = new MessageValidateException("uid", "desc");
        MessageAddException addException = new MessageAddException("uid");
        MessageNotFoundException notFoundException = new MessageNotFoundException("uid");

        assertThat(validateException).isInstanceOf(BaseMessageException.class);
        assertThat(addException).isInstanceOf(BaseMessageException.class);
        assertThat(notFoundException).isInstanceOf(BaseMessageException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}