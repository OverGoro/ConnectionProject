package com.connection.processing.buffer.mother;

import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;

public class BufferObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_DEVICE_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"); // Изменено
    private static final Integer DEFAULT_MAX_MESSAGES = 100;
    private static final Integer DEFAULT_MAX_SIZE = 1024;
    private static final String DEFAULT_PROTOTYPE = "message prototype";

    public static BufferDTO createValidBufferDTO() {
        return BufferDTO.builder()
                .uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferBLM createValidBufferBLM() {
        return BufferBLM.builder()
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDALM createValidBufferDALM() {
        return BufferDALM.builder()
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDTO createBufferDTOWithNullUid() {
        return BufferDTO.builder()
                .uid(null)
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDTO createBufferDTOWithInvalidUid() {
        return BufferDTO.builder()
                .uid("invalid-uuid")
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDTO createBufferDTOWithZeroMaxMessages() {
        return BufferDTO.builder()
                .uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(0)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDTO createBufferDTOWithNegativeMaxSize() {
        return BufferDTO.builder()
                .uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(-1)
                .messagePrototype(DEFAULT_PROTOTYPE)
                .build();
    }

    public static BufferDTO createBufferDTOWithEmptyPrototype() {
        return BufferDTO.builder()
                .uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype("")
                .build();
    }

    public static BufferBLM createBufferBLMWithNullFields() {
        return BufferBLM.builder()
                .uid(null)
                .deviceUid(null) // Изменено
                .maxMessagesNumber(null)
                .maxMessageSize(null)
                .messagePrototype(null)
                .build();
    }

    public static BufferDALM createBufferForDevice(UUID deviceUid) { // Изменено название метода
        return BufferDALM.builder()
                .uid(UUID.randomUUID())
                .deviceUid(deviceUid) // Изменено
                .maxMessagesNumber(50)
                .maxMessageSize(512)
                .messagePrototype("device-specific prototype")
                .build();
    }

    // Новый метод для тестирования связи со схемами соединений
    public static BufferDALM createBufferForConnectionSchemeTest(UUID deviceUid) {
        return BufferDALM.builder()
                .uid(UUID.randomUUID())
                .deviceUid(deviceUid)
                .maxMessagesNumber(75)
                .maxMessageSize(768)
                .messagePrototype("scheme-test-prototype")
                .build();
    }
}