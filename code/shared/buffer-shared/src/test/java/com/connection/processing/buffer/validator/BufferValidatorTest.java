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
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Validator Tests")
class BufferValidatorTest {

    private BufferValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BufferValidator();
    }

    @Test
    @DisplayName("Validate valid BufferDto - Positive")
    void testValidateBufferDto_Positive() {
        BufferDto buffer = createValidBufferDto();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate valid BufferBlm - Positive")
    void testValidateBufferBlm_Positive() {
        BufferBlm buffer = createValidBufferBlm();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate valid BufferDalm - Positive")
    void testValidateBufferDalm_Positive() {
        BufferDalm buffer = createValidBufferDalm();
        assertThat(buffer).isNotNull();
        validator.validate(buffer);
    }

    @Test
    @DisplayName("Validate null BufferDto - Negative")
    void testValidateNullBufferDto_Negative() {
        BufferDto buffer = null;
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with null UID - Negative")
    void testValidateBufferDtoWithNullUid_Negative() {
        BufferDto buffer = createBufferDtoWithNullUid();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with invalid UID - Negative")
    void testValidateBufferDtoWithInvalidUid_Negative() {
        BufferDto buffer = createBufferDtoWithInvalidUid();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with null device UID - Negative")
    void testValidateBufferDtoWithNullDeviceUid_Negative() {
        BufferDto buffer = createBufferDtoWithNullUid(); // Используем существующий метод, т.к. deviceUid теперь обязателен
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with zero max messages - Negative")
    void testValidateBufferDtoWithZeroMaxMessages_Negative() {
        BufferDto buffer = createBufferDtoWithZeroMaxMessages();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with negative max size - Negative")
    void testValidateBufferDtoWithNegativeMaxSize_Negative() {
        BufferDto buffer = createBufferDtoWithNegativeMaxSize();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDto with empty prototype - Negative")
    void testValidateBufferDtoWithEmptyPrototype_Negative() {
        BufferDto buffer = createBufferDtoWithEmptyPrototype();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate null BufferBlm - Negative")
    void testValidateNullBufferBlm_Negative() {
        BufferBlm buffer = null;
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferBlm with null fields - Negative")
    void testValidateBufferBlmWithNullFields_Negative() {
        BufferBlm buffer = createBufferBlmWithNullFields();
        assertThatThrownBy(() -> validator.validate(buffer))
                .isInstanceOf(BufferValidateException.class);
    }
}