package com.connection.scheme.validator;

import com.connection.scheme.exception.ConnectionSchemeValidateException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.model.ConnectionSchemeDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public class ConnectionSchemeValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** . */
    public void validate(ConnectionSchemeDto scheme) {
        validateNotNull(scheme, "Scheme");
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid(),
                    e.getMessage());
        }
    }

    /** . */
    public void validate(ConnectionSchemeBlm scheme) {
        validateNotNull(scheme, "Scheme");
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
            validateUsedBuffers(scheme.getUsedBuffers());
            validateBufferTransitions(scheme.getBufferTransitions());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(
                    getUidString(scheme.getUid()), e.getMessage());
        }
    }

    /** . */
    public void validate(ConnectionSchemeDalm scheme) {
        validateNotNull(scheme, "Scheme");
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
            validateUsedBuffers(scheme.getUsedBuffers());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(
                    getUidString(scheme.getUid()), e.getMessage());
        }
    }

    private void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new ConnectionSchemeValidateException("null",
                    fieldName + " is null");
        }
    }

    private String getUidString(UUID uid) {
        return uid != null ? uid.toString() : "null";
    }

    private void validateUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID cannot be empty");
        }
        validateUuidFormat(uid, "UID");
    }

    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID cannot be null");
        }
    }

    private void validateClientUid(String clientUid) {
        if (clientUid == null || clientUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Client UID cannot be empty");
        }
        validateUuidFormat(clientUid, "Client UID");
    }

    private void validateClientUid(UUID clientUid) {
        if (clientUid == null) {
            throw new IllegalArgumentException("Client UID cannot be null");
        }
    }

    private void validateUuidFormat(String uuidString, String fieldName) {
        try {
            UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + " format");
        }
    }

    private void validateSchemeJson(String schemeJson) {
        validateNonEmptyString(schemeJson, "Scheme JSON");
        validateJsonFormat(schemeJson);

        Map<UUID, List<UUID>> transitions = parseTransitions(schemeJson);
        validateTransitionsStructure(transitions);
    }

    private void validateNonEmptyString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    private void validateJsonFormat(String json) {
        if (!json.trim().startsWith("{")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }

    private Map<UUID, List<UUID>> parseTransitions(String schemeJson) {
        try {
            return objectMapper.readValue(schemeJson,
                    new TypeReference<Map<UUID, List<UUID>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid transitions JSON structure: " + e.getMessage());
        }
    }

    private void validateTransitionsStructure(
            Map<UUID, List<UUID>> transitions) {
        if (transitions.isEmpty()) {
            throw new IllegalArgumentException("Transitions cannot be empty");
        }

        for (Map.Entry<UUID, List<UUID>> entry : transitions.entrySet()) {
            validateTransitionEntry(entry);
        }
    }

    private void validateTransitionEntry(Map.Entry<UUID, List<UUID>> entry) {
        validateTransitionKey(entry.getKey());
        validateTransitionValueList(entry.getValue());
    }

    private void validateTransitionKey(UUID key) {
        if (key == null) {
            throw new IllegalArgumentException("Transition key cannot be null");
        }
    }

    private void validateTransitionValueList(List<UUID> valueList) {
        if (valueList == null) {
            throw new IllegalArgumentException(
                    "Transition value list cannot be null");
        }

        for (UUID bufferUid : valueList) {
            validateBufferUid(bufferUid);
        }
    }

    private void validateBufferUid(UUID bufferUid) {
        if (bufferUid == null) {
            throw new IllegalArgumentException(
                    "Buffer UID in transition list cannot be null");
        }
    }

    private void validateUsedBuffers(List<UUID> usedBuffers) {
        if (usedBuffers == null) {
            throw new IllegalArgumentException("Used buffers cannot be null");
        }
        // usedBuffers может быть пустым, но не null
    }

    private void validateBufferTransitions(
            Map<UUID, List<UUID>> bufferTransitions) {
        if (bufferTransitions == null) {
            throw new IllegalArgumentException(
                    "Buffer transitions cannot be null");
        }

        for (Map.Entry<UUID, List<UUID>> entry : bufferTransitions.entrySet()) {
            validateTransitionEntry(entry);
        }
    }
}
