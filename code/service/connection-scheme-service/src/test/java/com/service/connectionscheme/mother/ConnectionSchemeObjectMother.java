package com.service.connectionscheme.mother;

import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID SCHEME_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174003");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";
    public static final String SCHEME_JSON = "{\"devices\": [{\"id\": 1, \"name\": \"Device1\"}]}";

    public static ConnectionSchemeDTO createValidSchemeDTO() {
        return new ConnectionSchemeDTO(
            SCHEME_UUID.toString(),
            CLIENT_UUID.toString(),
            SCHEME_JSON
        );
    }

    public static ConnectionSchemeDTO createInvalidSchemeDTO() {
        return new ConnectionSchemeDTO(
            "invalid-uuid",
            "invalid-client-uuid",
            "" // empty json
        );
    }

    public static ConnectionSchemeBLM createValidSchemeBLM() {
        return new ConnectionSchemeBLM(
            SCHEME_UUID,
            CLIENT_UUID,
            SCHEME_JSON
        );
    }

    public static ConnectionSchemeDALM createValidSchemeDALM() {
        return new ConnectionSchemeDALM(
            SCHEME_UUID,
            CLIENT_UUID,
            SCHEME_JSON
        );
    }

    public static ConnectionSchemeDTO createSchemeDTOWithDifferentClient() {
        return new ConnectionSchemeDTO(
            SCHEME_UUID.toString(),
            UUID.randomUUID().toString(), // different client
            SCHEME_JSON
        );
    }

    public static ConnectionSchemeDTO createSchemeDTOWithDifferentUid() {
        return new ConnectionSchemeDTO(
            UUID.randomUUID().toString(), // different UID
            CLIENT_UUID.toString(),
            SCHEME_JSON
        );
    }
}