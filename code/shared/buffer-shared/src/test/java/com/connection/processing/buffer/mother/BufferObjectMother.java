package com.connection.processing.buffer.mother;

import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;
import java.util.UUID;

/** . */
public class BufferObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_DEVICE_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001"); // Изменено
    private static final Integer DEFAULT_MAX_MESSAGES = 100;
    private static final Integer DEFAULT_MAX_SIZE = 1024;
    private static final String DEFAULT_PROTOTYPE = "message prototype";

    /** . */
    public static BufferDto createValidBufferDto() {
        return BufferDto.builder().uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferBlm createValidBufferBlm() {
        return BufferBlm.builder().uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDalm createValidBufferDalm() {
        return BufferDalm.builder().uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithNullUid() {
        return BufferDto.builder().uid(null)
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithInvalidUid() {
        return BufferDto.builder().uid("invalid-uuid")
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithZeroMaxMessages() {
        return BufferDto.builder().uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(0).maxMessageSize(DEFAULT_MAX_SIZE)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithNegativeMaxSize() {
        return BufferDto.builder().uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES).maxMessageSize(-1)
                .messagePrototype(DEFAULT_PROTOTYPE).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithEmptyPrototype() {
        return BufferDto.builder().uid(DEFAULT_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString()) // Изменено
                .maxMessagesNumber(DEFAULT_MAX_MESSAGES)
                .maxMessageSize(DEFAULT_MAX_SIZE).messagePrototype("").build();
    }

    /** . */
    public static BufferBlm createBufferBlmWithNullFields() {
        return BufferBlm.builder().uid(null).deviceUid(null) // Изменено
                .maxMessagesNumber(null).maxMessageSize(null)
                .messagePrototype(null).build();
    }

    /** . */
    public static BufferDalm createBufferForDevice(UUID deviceUid) { // Изменено название метода
        return BufferDalm.builder().uid(UUID.randomUUID()).deviceUid(deviceUid) // Изменено
                .maxMessagesNumber(50).maxMessageSize(512)
                .messagePrototype("device-specific prototype").build();
    }

    // Новый метод для тестирования связи со схемами соединений
    /** . */
    public static BufferDalm createBufferForConnectionSchemeTest(
            UUID deviceUid) {
        return BufferDalm.builder().uid(UUID.randomUUID()).deviceUid(deviceUid)
                .maxMessagesNumber(75).maxMessageSize(768)
                .messagePrototype("scheme-test-prototype").build();
    }
}
