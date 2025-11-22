package com.connection.message.mother;

import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDalm;
import com.connection.message.model.MessageDto;
import java.util.Date;
import java.util.UUID;

/** . */
public class MessageObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID DEFAULT_BUFFER_UID =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    private static final String DEFAULT_CONTENT =
            "{\"type\":\"test\",\"data\":\"sample data\"}";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final Date DEFAULT_CREATED_AT =
            new Date(System.currentTimeMillis() - 1000L * 60 * 60); // 1 hour ago

    /** . */
    public static MessageDto createValidMessageDto() {
        return MessageDto.builder().uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID).content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE).createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    /** . */
    public static MessageBlm createValidMessageBlm() {
        return MessageBlm.builder().uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID).content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE).createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    /** . */
    public static MessageDalm createValidMessageDalm() {
        return MessageDalm.builder().uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID).content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE).createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    /** . */
    public static MessageDto createMessageDtoWithInvalidContent() {
        return MessageDto.builder().uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID).content("") // Пустой контент
                .contentType(DEFAULT_CONTENT_TYPE).createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    /** . */
    public static MessageDto createMessageDtoWithFutureDate() {
        return MessageDto.builder().uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID).content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(
                        new Date(System.currentTimeMillis() + 1000L * 60 * 60)) // Будущее время
                .build();
    }

    /** . */
    public static MessageDto createMessageDtoWithNullFields() {
        return MessageDto.builder().uid(null).bufferUid(null).content(null)
                .contentType(null).createdAt(null).build();
    }

    /** . */
    public static MessageDalm createMessageForBuffer(UUID bufferUid) {
        return MessageDalm.builder().uid(UUID.randomUUID()).bufferUid(bufferUid)
                .content("{\"buffer\":\"" + bufferUid
                        + "\",\"data\":\"test data\"}")
                .contentType(DEFAULT_CONTENT_TYPE).createdAt(new Date())
                .build();
    }

    /** . */
    public static MessageDalm createMessageWithContentType(String contentType) {
        return MessageDalm.builder().uid(UUID.randomUUID())
                .bufferUid(DEFAULT_BUFFER_UID)
                .content("{\"type\":\"custom\",\"contentType\":\"" + contentType
                        + "\"}")
                .contentType(contentType).createdAt(new Date()).build();
    }
}
