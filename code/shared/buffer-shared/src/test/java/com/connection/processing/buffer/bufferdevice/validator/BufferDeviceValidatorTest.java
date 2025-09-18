package com.connection.processing.buffer.bufferdevice.validator;

import static com.connection.processing.buffer.bufferdevice.mother.BufferDeviceObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceValidateException;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("BufferDevice Validator Tests")
class BufferDeviceValidatorTest {

    private BufferDeviceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BufferDeviceValidator();
    }

    @Test
    @DisplayName("Validate valid BufferDeviceDTO - Positive")
    void testValidateBufferDeviceDTO_Positive() {
        BufferDeviceDTO bufferDevice = createValidBufferDeviceDTO();
        assertThat(bufferDevice).isNotNull();
        validator.validate(bufferDevice);
    }

    @Test
    @DisplayName("Validate valid BufferDeviceBLM - Positive")
    void testValidateBufferDeviceBLM_Positive() {
        BufferDeviceBLM bufferDevice = createValidBufferDeviceBLM();
        assertThat(bufferDevice).isNotNull();
        validator.validate(bufferDevice);
    }

    @Test
    @DisplayName("Validate valid BufferDeviceDALM - Positive")
    void testValidateBufferDeviceDALM_Positive() {
        BufferDeviceDALM bufferDevice = createValidBufferDeviceDALM();
        assertThat(bufferDevice).isNotNull();
        validator.validate(bufferDevice);
    }

    @Test
    @DisplayName("Validate null BufferDeviceDTO - Negative")
    void testValidateNullBufferDeviceDTO_Negative() {
        BufferDeviceDTO bufferDevice = null;
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDeviceDTO with null buffer UID - Negative")
    void testValidateBufferDeviceDTOWithNullBufferUid_Negative() {
        BufferDeviceDTO bufferDevice = createBufferDeviceDTOWithNullBufferUid();
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDeviceDTO with null device UID - Negative")
    void testValidateBufferDeviceDTOWithNullDeviceUid_Negative() {
        BufferDeviceDTO bufferDevice = createBufferDeviceDTOWithNullDeviceUid();
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDeviceDTO with invalid buffer UID - Negative")
    void testValidateBufferDeviceDTOWithInvalidBufferUid_Negative() {
        BufferDeviceDTO bufferDevice = createBufferDeviceDTOWithInvalidBufferUid();
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDeviceDTO with invalid device UID - Negative")
    void testValidateBufferDeviceDTOWithInvalidDeviceUid_Negative() {
        BufferDeviceDTO bufferDevice = createBufferDeviceDTOWithInvalidDeviceUid();
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate null BufferDeviceBLM - Negative")
    void testValidateNullBufferDeviceBLM_Negative() {
        BufferDeviceBLM bufferDevice = null;
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferDeviceBLM with null fields - Negative")
    void testValidateBufferDeviceBLMWithNullFields_Negative() {
        BufferDeviceBLM bufferDevice = createBufferDeviceBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(bufferDevice))
                .isInstanceOf(BufferDeviceValidateException.class);
    }
}