package com.connection.processing.buffer.converter;

import static com.connection.processing.buffer.mother.BufferObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Converter Tests")
class BufferConverterTest {

    private BufferConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BufferConverter();
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        BufferDalm dalM = createValidBufferDalm();
        BufferBlm result = converter.toBlm(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(dalM.getDeviceUid()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(dalM.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(dalM.getMaxMessageSize());
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        BufferDto dto = createValidBufferDto();
        BufferBlm result = converter.toBlm(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getDeviceUid().toString()).isEqualTo(dto.getDeviceUid()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(dto.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(dto.getMaxMessageSize());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        BufferBlm blm = createValidBufferBlm();
        BufferDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid().toString()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(blm.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(blm.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(blm.getMessagePrototype());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        BufferBlm blm = createValidBufferBlm();
        BufferDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(blm.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(blm.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(blm.getMessagePrototype());
    }

    @Test
    @DisplayName("Round-trip conversion Dto -> Blm -> Dto")
    void testRoundTripDtoToBlmToDto() {
        BufferDto original = createValidBufferDto();
        BufferBlm blm = converter.toBlm(original);
        BufferDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(original.getDeviceUid()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(original.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(original.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(original.getMessagePrototype());
    }

    @Test
    @DisplayName("Round-trip conversion Dalm -> Blm -> Dalm")
    void testRoundTripDalmToBlmToDalm() {
        BufferDalm original = createValidBufferDalm();
        BufferBlm blm = converter.toBlm(original);
        BufferDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(original.getDeviceUid()); // Изменено
        assertThat(result.getMaxMessagesNumber()).isEqualTo(original.getMaxMessagesNumber());
        assertThat(result.getMaxMessageSize()).isEqualTo(original.getMaxMessageSize());
        assertThat(result.getMessagePrototype()).isEqualTo(original.getMessagePrototype());
    }
}