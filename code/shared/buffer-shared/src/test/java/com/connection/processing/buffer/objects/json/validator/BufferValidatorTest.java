package com.connection.processing.buffer.objects.json.validator;

import static com.connection.processing.buffer.objects.json.mother.BufferJsonDataObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.objects.json.exception.BufferJsonDataValidateException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Json Data Validator Tests")
class BufferJsonDataValidatorTest {

    private BufferJsonDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BufferJsonDataValidator();
    }

    @Test
    @DisplayName("Validate valid BufferJsonDataDTO - Positive")
    void testValidateBufferJsonDataDTO_Positive() {
        BufferJsonDataDTO data = createValidBufferJsonDataDTO();
        assertThat(data).isNotNull();
        validator.validate(data);
    }

    @Test
    @DisplayName("Validate valid BufferJsonDataBLM - Positive")
    void testValidateBufferJsonDataBLM_Positive() {
        BufferJsonDataBLM data = createValidBufferJsonDataBLM();
        assertThat(data).isNotNull();
        validator.validate(data);
    }

    @Test
    @DisplayName("Validate valid BufferJsonDataDALM - Positive")
    void testValidateBufferJsonDataDALM_Positive() {
        BufferJsonDataDALM data = createValidBufferJsonDataDALM();
        assertThat(data).isNotNull();
        validator.validate(data);
    }

    @Test
    @DisplayName("Validate null BufferJsonDataDTO - Negative")
    void testValidateNullBufferJsonDataDTO_Negative() {
        BufferJsonDataDTO data = null;
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with null UID - Negative")
    void testValidateBufferJsonDataDTOWithNullUid_Negative() {
        BufferJsonDataDTO data = createBufferJsonDataDTOWithNullUid();
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with invalid UID - Negative")
    void testValidateBufferJsonDataDTOWithInvalidUid_Negative() {
        BufferJsonDataDTO data = createBufferJsonDataDTOWithInvalidUid();
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with empty data - Negative")
    void testValidateBufferJsonDataDTOWithEmptyData_Negative() {
        BufferJsonDataDTO data = createBufferJsonDataDTOWithEmptyData();
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with null data - Negative")
    void testValidateBufferJsonDataDTOWithNullData_Negative() {
        BufferJsonDataDTO data = createBufferJsonDataDTOWithNullData();
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate null BufferJsonDataBLM - Negative")
    void testValidateNullBufferJsonDataBLM_Negative() {
        BufferJsonDataBLM data = null;
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataBLM with null fields - Negative")
    void testValidateBufferJsonDataBLMWithNullFields_Negative() {
        BufferJsonDataBLM data = createBufferJsonDataBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with invalid JSON - Negative")
    void testValidateBufferJsonDataDTOWithInvalidJson_Negative() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("invalid json")
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with array JSON - Negative")
    void testValidateBufferJsonDataDTOWithArrayJson_Negative() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("[1, 2, 3]")
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with future timestamp - Negative")
    void testValidateBufferJsonDataDTOWithFutureTimestamp_Negative() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("{\"key\": \"value\"}")
                .createdAt(Instant.now().plusSeconds(3600))
                .build();

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with null timestamp - Negative")
    void testValidateBufferJsonDataDTOWithNullTimestamp_Negative() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("{\"key\": \"value\"}")
                .createdAt(null)
                .build();

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with valid complex JSON - Positive")
    void testValidateBufferJsonDataDTOWithComplexJson_Positive() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("{\"name\": \"test\", \"values\": [1, 2, 3], \"nested\": {\"key\": \"value\"}}")
                .createdAt(Instant.now().minusSeconds(60))
                .build();

        assertThat(data).isNotNull();
        validator.validate(data);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with minimal valid JSON - Positive")
    void testValidateBufferJsonDataDTOWithMinimalJson_Positive() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("{}")
                .createdAt(Instant.now().minusSeconds(60))
                .build();

        assertThat(data).isNotNull();
        validator.validate(data);
    }

    @Test
    @DisplayName("Validate BufferJsonDataDTO with whitespace JSON - Negative")
    void testValidateBufferJsonDataDTOWithWhitespaceJson_Negative() {
        BufferJsonDataDTO data = BufferJsonDataDTO.builder()
                .uid("123e4567-e89b-12d3-a456-426614174000")
                .bufferUid("123e4567-e89b-12d3-a456-426614174001")
                .data("   ")
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(BufferJsonDataValidateException.class);
    }
}