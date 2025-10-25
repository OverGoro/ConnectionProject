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
import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.model.MessageDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Validator Tests")
class MessageValidatorTest {

    private MessageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MessageValidator();
    }

    @Test
    @DisplayName("Validate valid MessageDTO - Positive")
    void testValidateMessageDTO_Positive() {
        MessageDTO message = createValidMessageDTO();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate valid MessageBLM - Positive")
    void testValidateMessageBLM_Positive() {
        MessageBLM message = createValidMessageBLM();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate valid MessageDALM - Positive")
    void testValidateMessageDALM_Positive() {
        MessageDALM message = createValidMessageDALM();
        assertThat(message).isNotNull();
        validator.validate(message);
    }

    @Test
    @DisplayName("Validate null MessageDTO - Negative")
    void testValidateNullMessageDTO_Negative() {
        MessageDTO message = null;
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDTO with invalid content - Negative")
    void testValidateMessageDTOWithInvalidContent_Negative() {
        MessageDTO message = createMessageDTOWithInvalidContent();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDTO with future date - Negative")
    void testValidateMessageDTOWithFutureDate_Negative() {
        MessageDTO message = createMessageDTOWithFutureDate();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate null MessageBLM - Negative")
    void testValidateNullMessageBLM_Negative() {
        MessageBLM message = null;
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDTO with null fields - Negative")
    void testValidateMessageDTOWithNullFields_Negative() {
        MessageDTO message = createMessageDTOWithNullFields();
        assertThatThrownBy(() -> validator.validate(message))
                .isInstanceOf(MessageValidateException.class);
    }

    @Test
    @DisplayName("Validate MessageDTO with empty content - Negative")
    void testValidateMessageDTOWithEmptyContent_Negative() {
        MessageDTO message = MessageDTO.builder()
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
    @DisplayName("Validate MessageDTO with long content - Negative")
    void testValidateMessageDTOWithLongContent_Negative() {
        String longContent = "a".repeat(10001);
        MessageDTO message = MessageDTO.builder()
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
    @DisplayName("Validate MessageDTO with null buffer UID - Negative")
    void testValidateMessageDTOWithNullBufferUid_Negative() {
        MessageDTO message = MessageDTO.builder()
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
    @DisplayName("Validate MessageDTO with null content type - Negative")
    void testValidateMessageDTOWithNullContentType_Negative() {
        MessageDTO message = MessageDTO.builder()
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
    @DisplayName("Validate MessageDTO with long content type - Negative")
    void testValidateMessageDTOWithLongContentType_Negative() {
        String longContentType = "a".repeat(101);
        MessageDTO message = MessageDTO.builder()
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
    @DisplayName("Validate MessageDTO with null created at - Negative")
    void testValidateMessageDTOWithNullCreatedAt_Negative() {
        MessageDTO message = MessageDTO.builder()
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