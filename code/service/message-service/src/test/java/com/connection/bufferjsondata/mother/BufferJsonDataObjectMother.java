package com.connection.bufferjsondata.mother;

import java.time.Instant;
import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

public class BufferJsonDataObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID BUFFER_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
    public static final UUID DATA_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final UUID CONNECTION_SCHEME_UUID = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";
    public static final String JSON_DATA = "{\"key\": \"value\", \"number\": 42}";
    public static final Instant CREATED_AT = Instant.now();

    public static BufferJsonDataDTO createValidBufferJsonDataDTO() {
        return new BufferJsonDataDTO(
            DATA_UUID.toString(),
            BUFFER_UUID.toString(),
            JSON_DATA,
            CREATED_AT
        );
    }

    public static BufferJsonDataBLM createValidBufferJsonDataBLM() {
        return new BufferJsonDataBLM(
            DATA_UUID,
            BUFFER_UUID,
            JSON_DATA,
            CREATED_AT
        );
    }

    public static BufferJsonDataDALM createValidBufferJsonDataDALM() {
        return new BufferJsonDataDALM(
            DATA_UUID,
            BUFFER_UUID,
            JSON_DATA,
            CREATED_AT
        );
    }

    public static BufferBLM createValidBufferBLM() {
        return new BufferBLM(
            BUFFER_UUID,
            CONNECTION_SCHEME_UUID,
            1000, // maxMessagesNumber
            1024, // maxMessageSize
            "{}"  // messagePrototype
        );
    }

    public static BufferJsonDataDTO createInvalidBufferJsonDataDTO() {
        return new BufferJsonDataDTO(
            "invalid-uuid",
            "invalid-buffer-uuid",
            "", // empty data
            null
        );
    }
}