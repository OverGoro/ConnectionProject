package com.connection.processing.buffer.converter;

import static com.connection.processing.buffer.mother.BufferObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Converter Tests")
class BufferConverterTest {

    private BufferConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BufferConverter();
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        BufferDALM dalM = createValidBufferDALM();
        BufferBLM result = converter.toBLM(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getConnectionSchemeUid()).isEqualTo(dalM.getConnectionSchemeUid());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(dalM.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(dalM.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(dalM.getMessagePrototype());
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        BufferDTO dto = createValidBufferDTO();
        BufferBLM result = converter.toBLM(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getConnectionSchemeUid().toString()).isEqualTo(dto.getConnectionSchemeUid());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(dto.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(dto.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(dto.getMessagePrototype());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        BufferBLM blm = createValidBufferBLM();
        BufferDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getConnectionSchemeUid()).isEqualTo(blm.getConnectionSchemeUid().toString());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(blm.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(blm.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(blm.getMessagePrototype());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        BufferBLM blm = createValidBufferBLM();
        BufferDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getConnectionSchemeUid()).isEqualTo(blm.getConnectionSchemeUid());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(blm.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(blm.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(blm.getMessagePrototype());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        BufferDTO original = createValidBufferDTO();
        BufferBLM blm = converter.toBLM(original);
        BufferDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getConnectionSchemeUid()).isEqualTo(original.getConnectionSchemeUid());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(original.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(original.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(original.getMessagePrototype());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        BufferDALM original = createValidBufferDALM();
        BufferBLM blm = converter.toBLM(original);
        BufferDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getConnectionSchemeUid()).isEqualTo(original.getConnectionSchemeUid());
        assertThat(result.getMaxMessagesNumber()).isEqualTo(original.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(original.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(original.getMessagePrototype());
    }
}