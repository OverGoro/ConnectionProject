package com.connection.processing.buffer.bufferdevice.converter;

import static com.connection.processing.buffer.bufferdevice.mother.BufferDeviceObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("BufferDevice Converter Tests")
class BufferDeviceConverterTest {

    private BufferDeviceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BufferDeviceConverter();
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        BufferDeviceDALM dalM = createValidBufferDeviceDALM();
        BufferDeviceBLM result = converter.toBLM(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(dalM.getBufferUid());
        assertThat(result.getDeviceUid()).isEqualTo(dalM.getDeviceUid());
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        BufferDeviceDTO dto = createValidBufferDeviceDTO();
        BufferDeviceBLM result = converter.toBLM(dto);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid().toString()).isEqualTo(dto.getBufferUid());
        assertThat(result.getDeviceUid().toString()).isEqualTo(dto.getDeviceUid());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        BufferDeviceBLM blm = createValidBufferDeviceBLM();
        BufferDeviceDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid().toString());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid().toString());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        BufferDeviceBLM blm = createValidBufferDeviceBLM();
        BufferDeviceDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        BufferDeviceDTO original = createValidBufferDeviceDTO();
        BufferDeviceBLM blm = converter.toBLM(original);
        BufferDeviceDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getDeviceUid()).isEqualTo(original.getDeviceUid());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        BufferDeviceDALM original = createValidBufferDeviceDALM();
        BufferDeviceBLM blm = converter.toBLM(original);
        BufferDeviceDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getDeviceUid()).isEqualTo(original.getDeviceUid());
    }
}