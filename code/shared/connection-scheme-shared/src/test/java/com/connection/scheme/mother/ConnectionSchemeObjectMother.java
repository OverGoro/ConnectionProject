// ConnectionSchemeObjectMother.java
package com.connection.scheme.mother;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID BUFFER_UID_1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    private static final UUID BUFFER_UID_2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174003");
    private static final UUID BUFFER_UID_3 = UUID.fromString("223e4567-e89b-12d3-a456-426614174004");
    
    private static final String DEFAULT_SCHEME_JSON = "{" +
        "\"usedBuffers\": [\"" + BUFFER_UID_1 + "\", \"" + BUFFER_UID_2 + "\"], " +
        "\"bufferTransitions\": {" +
            "\"" + BUFFER_UID_1 + "\": [\"" + BUFFER_UID_2 + "\"], " +
            "\"" + BUFFER_UID_2 + "\": [\"" + BUFFER_UID_3 + "\"]" +
        "}" +
    "}";

    public static ConnectionSchemeDTO createValidConnectionSchemeDTO() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .build();
    }

    public static ConnectionSchemeBLM createValidConnectionSchemeBLM() {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UID_1, Arrays.asList(BUFFER_UID_2));
        bufferTransitions.put(BUFFER_UID_2, Arrays.asList(BUFFER_UID_3));
        
        return ConnectionSchemeBLM.builder()
                .uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2))
                .bufferTransitions(bufferTransitions)
                .build();
    }

    public static ConnectionSchemeDALM createValidConnectionSchemeDALM() {
        return ConnectionSchemeDALM.builder()
                .uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2))
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithNullUid() {
        return ConnectionSchemeDTO.builder()
                .uid(null)
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithInvalidUid() {
        return ConnectionSchemeDTO.builder()
                .uid("invalid-uuid")
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithEmptyJson() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("")
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithInvalidJson() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("invalid json")
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithMissingUsedBuffers() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("{\"bufferTransitions\": {}}")
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithMissingBufferTransitions() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("{\"usedBuffers\": []}")
                .build();
    }

    public static ConnectionSchemeBLM createConnectionSchemeBLMWithNullFields() {
        return ConnectionSchemeBLM.builder()
                .uid(null)
                .clientUid(null)
                .schemeJson(null)
                .usedBuffers(null)
                .bufferTransitions(null)
                .build();
    }

    public static ConnectionSchemeDALM createConnectionSchemeForClient(UUID clientUid) {
        String clientSpecificJson = "{" +
            "\"usedBuffers\": [\"" + BUFFER_UID_1 + "\"], " +
            "\"bufferTransitions\": {\"" + BUFFER_UID_1 + "\": []}" +
        "}";
        
        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(clientUid)
                .schemeJson(clientSpecificJson)
                .usedBuffers(Arrays.asList(BUFFER_UID_1))
                .build();
    }

    public static ConnectionSchemeDALM createConnectionSchemeDALMWithUsedBuffers(List<UUID> usedBuffers) {
        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(usedBuffers)
                .build();
    }
}