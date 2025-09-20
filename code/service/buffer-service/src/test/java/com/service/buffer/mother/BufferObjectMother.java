package com.service.buffer.mother;

import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.scheme.model.ConnectionSchemeBLM;

public class BufferObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID BUFFER_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final UUID SCHEME_UUID = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";

    public static BufferDTO createValidBufferDTO() {
        return new BufferDTO(
            BUFFER_UUID.toString(),
            SCHEME_UUID.toString(),
            1000,
            1024,
            "message prototype"
        );
    }

    public static BufferBLM createValidBufferBLM() {
        return new BufferBLM(
            BUFFER_UUID,
            SCHEME_UUID,
            1000,
            1024,
            "message prototype"
        );
    }

    public static BufferDALM createValidBufferDALM() {
        return new BufferDALM(
            BUFFER_UUID,
            SCHEME_UUID,
            1000,
            1024,
            "message prototype"
        );
    }

    public static ConnectionSchemeBLM createValidConnectionSchemeBLM() {
        return new ConnectionSchemeBLM(
            SCHEME_UUID,
            CLIENT_UUID,
            "{\"scheme\": \"test\"}"
        );
    }

    public static BufferDTO createBufferDTOWithDifferentScheme() {
        return new BufferDTO(
            BUFFER_UUID.toString(),
            UUID.randomUUID().toString(), // different scheme
            1000,
            1024,
            "message prototype"
        );
    }

    public static BufferDTO createInvalidBufferDTO() {
        return new BufferDTO(
            "invalid-uuid",
            "invalid-scheme-uuid",
            -1, // invalid max messages
            -1, // invalid max size
            null // null prototype
        );
    }
}