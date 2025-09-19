package com.connection.scheme.mother;

import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_SCHEME_JSON = "{\"connections\": [], \"devices\": []}";

    public static ConnectionSchemeDTO createValidConnectionSchemeDTO() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .build();
    }

    public static ConnectionSchemeBLM createValidConnectionSchemeBLM() {
        return ConnectionSchemeBLM.builder()
                .uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .build();
    }

    public static ConnectionSchemeDALM createValidConnectionSchemeDALM() {
        return ConnectionSchemeDALM.builder()
                .uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
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

    public static ConnectionSchemeBLM createConnectionSchemeBLMWithNullFields() {
        return ConnectionSchemeBLM.builder()
                .uid(null)
                .clientUid(null)
                .schemeJson(null)
                .build();
    }

    public static ConnectionSchemeDALM createConnectionSchemeForClient(UUID clientUid) {
        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(clientUid)
                .schemeJson("{\"client\": \"" + clientUid + "\", \"connections\": []}")
                .build();
    }
}