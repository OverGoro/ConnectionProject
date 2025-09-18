package com.connection.processing.buffer.validator;

import static com.connection.processing.buffer.mother.BufferObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.exception.BufferValidateException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Validator Tests")
class BufferValidatorTest {

    private BufferValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BufferValidator();
    }

    @Test
    @DisplayName("Validate valid BufferDTO - Positive")
    void testValidateBufferDTO_Positive() {
        BufferDTO buffer = createValidBufferDTO();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate valid BufferBLM - Positive")
    void testValidateBufferBLM_Positive() {
        BufferBLM buffer = createValidBufferBLM();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate valid BufferDALM - Positive")
    void testValidateBufferDALM_Positive() {
        BufferDALM buffer = createValidBufferDALM();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate null BufferDTO - Negative")
    void testValidateNullBufferDTO_Negative() {
        BufferDTO buffer = null;
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDTO with null UID - Negative")
    void testValidateBufferDTOWithNullUid_Negative() {
        BufferDTO buffer = createBufferDTOWithNullUid();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDTO with invalid UID - Negative")
    void testValidateBufferDTOWithInvalidUid_Negative() {
        BufferDTO buffer = createBufferDTOWithInvalidUid();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDTO with zero max messages - Negative")
    void testValidateBufferDTOWithZeroMaxMessages_Negative() {
        BufferDTO buffer = createBufferDTOWithZeroMaxMessages();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDTO with negative max size - Negative")
    void testValidateBufferDTOWithNegativeMaxSize_Negative() {
        BufferDTO buffer = createBufferDTOWithNegativeMaxSize();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDTO with empty prototype - Negative")
    void testValidateBufferDTOWithEmptyPrototype_Negative() {
        BufferDTO buffer = createBufferDTOWithEmptyPrototype();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate null BufferBLM - Negative")
    void testValidateNullBufferBLM_Negative() {
        BufferBLM buffer = null;
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferBLM with null fields - Negative")
    void testValidateBufferBLMWithNullFields_Negative() {
        BufferBLM buffer = createBufferBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }
}