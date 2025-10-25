// Object Mother для тестов
package com.connection.message.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.model.MessageDTO;

public class MessageObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID DEFAULT_BUFFER_UID = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    private static final String DEFAULT_CONTENT = "{\"type\":\"test\",\"data\":\"sample data\"}";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final Date DEFAULT_CREATED_AT = new Date(System.currentTimeMillis() - 1000L * 60 * 60); // 1 hour ago

    public static MessageDTO createValidMessageDTO() {
        return MessageDTO.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static MessageBLM createValidMessageBLM() {
        return MessageBLM.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static MessageDALM createValidMessageDALM() {
        return MessageDALM.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static MessageDTO createMessageDTOWithInvalidContent() {
        return MessageDTO.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .content("") // Пустой контент
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static MessageDTO createMessageDTOWithFutureDate() {
        return MessageDTO.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .content(DEFAULT_CONTENT)
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60)) // Будущее время
                .build();
    }

    public static MessageDTO createMessageDTOWithNullFields() {
        return MessageDTO.builder()
                .uid(null)
                .bufferUid(null)
                .content(null)
                .contentType(null)
                .createdAt(null)
                .build();
    }

    public static MessageDALM createMessageForBuffer(UUID bufferUid) {
        return MessageDALM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(bufferUid)
                .content("{\"buffer\":\"" + bufferUid + "\",\"data\":\"test data\"}")
                .contentType(DEFAULT_CONTENT_TYPE)
                .createdAt(new Date())
                .build();
    }

    public static MessageDALM createMessageWithContentType(String contentType) {
        return MessageDALM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(DEFAULT_BUFFER_UID)
                .content("{\"type\":\"custom\",\"contentType\":\"" + contentType + "\"}")
                .contentType(contentType)
                .createdAt(new Date())
                .build();
    }
}