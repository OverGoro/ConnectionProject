package com.connection.processing.buffer.objects.json.converter;

import static com.connection.processing.buffer.objects.json.mother.BufferJsonDataObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Json Data Converter Tests")
class BufferJsonDataConverterTest {

    private BufferJsonDataConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BufferJsonDataConverter();
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        BufferJsonDataDALM dalM = createValidBufferJsonDataDALM();
        BufferJsonDataBLM result = converter.toBLM(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getBufferUid()).isEqualTo(dalM.getBufferUid());
        assertThat(result.getData()).isEqualTo(dalM.getData());
        assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        BufferJsonDataDTO dto = createValidBufferJsonDataDTO();
        BufferJsonDataBLM result = converter.toBLM(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getBufferUid().toString()).isEqualTo(dto.getBufferUid());
        assertThat(result.getData()).isEqualTo(dto.getData());
        assertThat(result.getCreatedAt()).isEqualTo(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        BufferJsonDataBLM blm = createValidBufferJsonDataBLM();
        BufferJsonDataDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid().toString());
        assertThat(result.getData()).isEqualTo(blm.getData());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        BufferJsonDataBLM blm = createValidBufferJsonDataBLM();
        BufferJsonDataDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getData()).isEqualTo(blm.getData());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        BufferJsonDataDTO original = createValidBufferJsonDataDTO();
        BufferJsonDataBLM blm = converter.toBLM(original);
        BufferJsonDataDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getData()).isEqualTo(original.getData());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        BufferJsonDataDALM original = createValidBufferJsonDataDALM();
        BufferJsonDataBLM blm = converter.toBLM(original);
        BufferJsonDataDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getData()).isEqualTo(original.getData());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}