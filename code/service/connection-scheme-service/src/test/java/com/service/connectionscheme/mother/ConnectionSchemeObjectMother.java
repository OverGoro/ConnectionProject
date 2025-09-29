package com.service.connectionscheme.mother;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID SCHEME_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174003");
    public static final UUID BUFFER_UUID_1 = UUID.fromString("423e4567-e89b-12d3-a456-426614174004");
    public static final UUID BUFFER_UUID_2 = UUID.fromString("523e4567-e89b-12d3-a456-426614174005");
    public static final UUID BUFFER_UUID_3 = UUID.fromString("623e4567-e89b-12d3-a456-426614174006");
    
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";
    
    public static final String SCHEME_JSON = "{" +
        "\"usedBuffers\": [\"" + BUFFER_UUID_1 + "\", \"" + BUFFER_UUID_2 + "\"], " +
        "\"bufferTransitions\": {" +
            "\"" + BUFFER_UUID_1 + "\": [\"" + BUFFER_UUID_2 + "\"], " +
            "\"" + BUFFER_UUID_2 + "\": [\"" + BUFFER_UUID_3 + "\"]" +
        "}" +
    "}";

    public static ConnectionSchemeDTO createValidSchemeDTO() {
        return ConnectionSchemeDTO.builder()
            .uid(SCHEME_UUID.toString())
            .clientUid(CLIENT_UUID.toString())
            .schemeJson(SCHEME_JSON)
            .build();
    }

    public static ConnectionSchemeDTO createInvalidSchemeDTO() {
        return ConnectionSchemeDTO.builder()
            .uid("invalid-uuid")
            .clientUid("invalid-client-uuid")
            .schemeJson("") // empty json
            .build();
    }

    public static ConnectionSchemeDTO createInvalidJsonStructureSchemeDTO() {
        return ConnectionSchemeDTO.builder()
            .uid(SCHEME_UUID.toString())
            .clientUid(CLIENT_UUID.toString())
            .schemeJson("{\"invalid\": \"structure\"}") // missing required fields
            .build();
    }

    public static ConnectionSchemeBLM createValidSchemeBLM() {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UUID_1, Arrays.asList(BUFFER_UUID_2));
        bufferTransitions.put(BUFFER_UUID_2, Arrays.asList(BUFFER_UUID_3));
        
        return ConnectionSchemeBLM.builder()
            .uid(SCHEME_UUID)
            .clientUid(CLIENT_UUID)
            .schemeJson(SCHEME_JSON)
            .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2))
            .bufferTransitions(bufferTransitions)
            .build();
    }

    public static ConnectionSchemeBLM createSchemeBLMWithEmptyBuffers() {
        return ConnectionSchemeBLM.builder()
            .uid(SCHEME_UUID)
            .clientUid(CLIENT_UUID)
            .schemeJson("{\"usedBuffers\": [], \"bufferTransitions\": {}}")
            .usedBuffers(Arrays.asList())
            .bufferTransitions(new HashMap<>())
            .build();
    }

    public static ConnectionSchemeDALM createValidSchemeDALM() {
        return ConnectionSchemeDALM.builder()
            .uid(SCHEME_UUID)
            .clientUid(CLIENT_UUID)
            .schemeJson(SCHEME_JSON)
            .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2))
            .build();
    }

    public static ConnectionSchemeDALM createSchemeDALMWithUsedBuffers(List<UUID> usedBuffers) {
        return ConnectionSchemeDALM.builder()
            .uid(SCHEME_UUID)
            .clientUid(CLIENT_UUID)
            .schemeJson(SCHEME_JSON)
            .usedBuffers(usedBuffers)
            .build();
    }

    public static ConnectionSchemeDTO createSchemeDTOWithDifferentClient() {
        return ConnectionSchemeDTO.builder()
            .uid(SCHEME_UUID.toString())
            .clientUid(UUID.randomUUID().toString()) // different client
            .schemeJson(SCHEME_JSON)
            .build();
    }

    public static ConnectionSchemeDTO createSchemeDTOWithDifferentUid() {
        return ConnectionSchemeDTO.builder()
            .uid(UUID.randomUUID().toString()) // different UID
            .clientUid(CLIENT_UUID.toString())
            .schemeJson(SCHEME_JSON)
            .build();
    }

    public static ConnectionSchemeDTO createSchemeDTOWithNullFields() {
        return ConnectionSchemeDTO.builder()
            .uid(null)
            .clientUid(null)
            .schemeJson(null)
            .build();
    }

    public static ConnectionSchemeBLM createSchemeBLMWithNullFields() {
        return ConnectionSchemeBLM.builder()
            .uid(null)
            .clientUid(null)
            .schemeJson(null)
            .usedBuffers(null)
            .bufferTransitions(null)
            .build();
    }

    public static ConnectionSchemeDALM createSchemeDALMWithNullFields() {
        return ConnectionSchemeDALM.builder()
            .uid(null)
            .clientUid(null)
            .schemeJson(null)
            .usedBuffers(null)
            .build();
    }

    public static ConnectionSchemeDTO createSchemeDTOForBufferTest(UUID bufferUid) {
        String bufferSpecificJson = "{" +
            "\"usedBuffers\": [\"" + bufferUid + "\"], " +
            "\"bufferTransitions\": {\"" + bufferUid + "\": []}" +
        "}";
        
        return ConnectionSchemeDTO.builder()
            .uid(UUID.randomUUID().toString())
            .clientUid(CLIENT_UUID.toString())
            .schemeJson(bufferSpecificJson)
            .build();
    }
}