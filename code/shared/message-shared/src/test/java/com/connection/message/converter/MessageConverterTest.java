package com.connection.message.converter;

import static com.connection.message.mother.MessageObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDalm;
import com.connection.message.model.MessageDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Converter Tests")
class  MessageConverterTest{

    private MessageConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MessageConverter();
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        MessageDto dto = createValidMessageDto();
        MessageBlm result = converter.toBlm(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dto.getUid());
        assertThat(result.getBufferUid()).isEqualTo(dto.getBufferUid());
        assertThat(result.getContent()).isEqualTo(dto.getContent());
        assertThat(result.getContentType()).isEqualTo(dto.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        MessageDalm dalM = createValidMessageDalm();
        MessageBlm result = converter.toBlm(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getBufferUid()).isEqualTo(dalM.getBufferUid());
        assertThat(result.getContent()).isEqualTo(dalM.getContent());
        assertThat(result.getContentType()).isEqualTo(dalM.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        MessageBlm blm = createValidMessageBlm();
        MessageDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getContent()).isEqualTo(blm.getContent());
        assertThat(result.getContentType()).isEqualTo(blm.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        MessageBlm blm = createValidMessageBlm();
        MessageDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBufferUid()).isEqualTo(blm.getBufferUid());
        assertThat(result.getContent()).isEqualTo(blm.getContent());
        assertThat(result.getContentType()).isEqualTo(blm.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion Dto -> Blm -> Dto")
    void testRoundTripDtoToBlmToDto() {
        MessageDto original = createValidMessageDto();
        MessageBlm blm = converter.toBlm(original);
        MessageDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getContent()).isEqualTo(original.getContent());
        assertThat(result.getContentType()).isEqualTo(original.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    @Test
    @DisplayName("Round-trip conversion Dalm -> Blm -> Dalm")
    void testRoundTripDalmToBlmToDalm() {
        MessageDalm original = createValidMessageDalm();
        MessageBlm blm = converter.toBlm(original);
        MessageDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBufferUid()).isEqualTo(original.getBufferUid());
        assertThat(result.getContent()).isEqualTo(original.getContent());
        assertThat(result.getContentType()).isEqualTo(original.getContentType());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}