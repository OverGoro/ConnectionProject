package com.service.buffer.mother;

import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;
import com.connection.scheme.model.ConnectionSchemeBlm;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public class BufferObjectMother {

    public static final UUID CLIENT_UUID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID BUFFER_UUID =
            UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final UUID SCHEME_UUID =
            UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";

    private static final UUID BUFFER_UID_1 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    private static final UUID BUFFER_UID_2 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174003");
    private static final UUID BUFFER_UID_3 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174004");

    private static final String DEFAULT_SCHEME_JSON = "{"
            + "\"usedBuffers\": [\"" + BUFFER_UID_1 + "\", \"" + BUFFER_UID_2
            + "\"], " + "\"bufferTransitions\": {" + "\"" + BUFFER_UID_1
            + "\": [\"" + BUFFER_UID_2 + "\"], " + "\"" + BUFFER_UID_2
            + "\": [\"" + BUFFER_UID_3 + "\"]" + "}" + "}";

    /** . */
    public static BufferDto createValidBufferDto() {
        return new BufferDto(BUFFER_UUID.toString(), SCHEME_UUID.toString(),
                1000, 1024, "message prototype");
    }

    /** . */
    public static BufferBlm createValidBufferBlm() {
        return new BufferBlm(BUFFER_UUID, SCHEME_UUID, 1000, 1024,
                "message prototype");
    }

    /** . */
    public static BufferDalm createValidBufferDalm() {
        return new BufferDalm(BUFFER_UUID, SCHEME_UUID, 1000, 1024,
                "message prototype");
    }

    /** . */
    public static ConnectionSchemeBlm createValidConnectionSchemeBlm() {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UID_1, Arrays.asList(BUFFER_UID_2));
        bufferTransitions.put(BUFFER_UID_2, Arrays.asList(BUFFER_UID_3));

        return ConnectionSchemeBlm.builder().uid(SCHEME_UUID)
                .clientUid(CLIENT_UUID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2))
                .bufferTransitions(bufferTransitions).build();
    }

    /** . */
    public static ConnectionSchemeBlm createValidConnectionSchemeBlm(
            UUID clientUuid) {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UID_1, Arrays.asList(BUFFER_UID_2));
        bufferTransitions.put(BUFFER_UID_2, Arrays.asList(BUFFER_UID_3));

        return ConnectionSchemeBlm.builder().uid(SCHEME_UUID)
                .clientUid(clientUuid).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2))
                .bufferTransitions(bufferTransitions).build();
    }

    /** . */
    public static BufferDto createBufferDtoWithDifferentScheme() {
        return new BufferDto(BUFFER_UUID.toString(),
                UUID.randomUUID().toString(), // different scheme
                1000, 1024, "message prototype");
    }

    /** . */
    public static BufferDto createInvalidBufferDto() {
        return new BufferDto("invalid-uuid", "invalid-scheme-uuid", -1, // invalid max messages
                -1, // invalid max size
                null // null prototype
        );
    }
}
