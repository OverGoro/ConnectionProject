package com.connection.message.validator;

import static com.connection.message.mother.MessageObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.message.exception.MessageValidateException;
import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDalm;
import com.connection.message.model.MessageDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Validator Tests")
class MessageValidatorTest {

    private MessageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MessageValidator();
    }

    @Test
    @DisplayName("Validate valid MessageDto - Positive")
    void testValidateMessageDto_Positive() {
        MessageDto message = createValidMessageDto();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate valid MessageBlm - Positive")
    void testValidateMessageBlm_Positive() {
        MessageBlm message = createValidMessageBlm();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate valid MessageDalm - Positive")
    void testValidateMessageDalm_Positive() {
        MessageDalm message = createValidMessageDalm();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate null MessageDto - Negative")
    void testValidateNullMessageDto_Negative() {
        MessageDto message = null;
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with invalid content - Negative")
    void testValidateMessageDtoWithInvalidContent_Negative() {
        MessageDto message = createMessageDtoWithInvalidContent();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with future date - Negative")
    void testValidateMessageDtoWithFutureDate_Negative() {
        MessageDto message = createMessageDtoWithFutureDate();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate null MessageBlm - Negative")
    void testValidateNullMessageBlm_Negative() {
        MessageBlm message = null;
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with null fields - Negative")
    void testValidateMessageDtoWithNullFields_Negative() {
        MessageDto message = createMessageDtoWithNullFields();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with empty content - Negative")
    void testValidateMessageDtoWithEmptyContent_Negative() {
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(UUID.randomUUID())
                .content("")
                .contentType("application/json")
                .createdAt(new Date())
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with long content - Negative")
    void testValidateMessageDtoWithLongContent_Negative() {
        String longContent = "a".repeat(10001);
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(UUID.randomUUID())
                .content(longContent)
                .contentType("application/json")
                .createdAt(new Date())
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with null buffer UID - Negative")
    void testValidateMessageDtoWithNullBufferUid_Negative() {
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(null)
                .content("{\"test\":\"data\"}")
                .contentType("application/json")
                .createdAt(new Date())
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with null content type - Negative")
    void testValidateMessageDtoWithNullContentType_Negative() {
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(UUID.randomUUID())
                .content("{\"test\":\"data\"}")
                .contentType(null)
                .createdAt(new Date())
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with long content type - Negative")
    void testValidateMessageDtoWithLongContentType_Negative() {
        String longContentType = "a".repeat(101);
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(UUID.randomUUID())
                .content("{\"test\":\"data\"}")
                .contentType(longContentType)
                .createdAt(new Date())
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDto with null created at - Negative")
    void testValidateMessageDtoWithNullCreatedAt_Negative() {
        MessageDto message = MessageDto.builder()
                .uid(UUID.randomUUID())
                .bufferUid(UUID.randomUUID())
                .content("{\"test\":\"data\"}")
                .contentType("application/json")
                .createdAt(null)
                .build();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }
}