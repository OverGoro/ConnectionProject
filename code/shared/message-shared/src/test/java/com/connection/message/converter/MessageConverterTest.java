package com.connection.message.converter;

import static com.connection.message.mother.MessageObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.model.MessageDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Converter Tests")
class  MessageConverterTest{

    private MessageConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MessageConverter();
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        MessageDTO dto = createValidMessageDTO();
        MessageBLM result = converter.toBLM(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dto.getUid());
        assertThat(result.getBufferUid()).isEqualTo(dto.getBufferUid());
        assertThat(result.getContent()).isEqualTo(dto.getContent());
        assertThat(result.getContentType()).isEqualTo(dto.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        MessageDALM dalM = createValidMessageDALM();
        MessageBLM result = converter.toBLM(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getBufferUid()).isEqualTo(dalM.getBufferUid());
        assertThat(result.getContent()).isEqualTo(dalM.getContent());
        assertThat(result.getContentType()).isEqualTo(dalM.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        MessageBLM blm = createValidMessageBLM();
        MessageDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getContent()).isEqualTo(blm.getContent());
        assertThat(result.getContentType()).isEqualTo(blm.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        MessageBLM blm = createValidMessageBLM();
        MessageDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getContent()).isEqualTo(blm.getContent());
        assertThat(result.getContentType()).isEqualTo(blm.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        MessageDTO original = createValidMessageDTO();
        MessageBLM blm = converter.toBLM(original);
        MessageDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getContent()).isEqualTo(original.getContent());
        assertThat(result.getContentType()).isEqualTo(original.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        MessageDALM original = createValidMessageDALM();
        MessageBLM blm = converter.toBLM(original);
        MessageDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getContent()).isEqualTo(original.getContent());
        assertThat(result.getContentType()).isEqualTo(original.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}