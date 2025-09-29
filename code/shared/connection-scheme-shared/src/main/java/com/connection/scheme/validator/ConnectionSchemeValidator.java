// ConnectionSchemeValidator.java
package com.connection.scheme.validator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.connection.scheme.exception.ConnectionSchemeValidateException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeValidator {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public void validate(ConnectionSchemeDTO scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid(), e.getMessage());
        }
    }

    public void validate(ConnectionSchemeBLM scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
            validateUsedBuffers(scheme.getUsedBuffers());
            validateBufferTransitions(scheme.getBufferTransitions());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid() != null ? scheme.getUid().toString() : "null", e.getMessage());
        }
    }

    public void validate(ConnectionSchemeDALM scheme) {
        if (scheme == null) {
            throw new ConnectionSchemeValidateException("null", "Scheme is null");
        }
        try {
            validateUid(scheme.getUid());
            validateClientUid(scheme.getClientUid());
            validateSchemeJson(scheme.getSchemeJson());
            validateUsedBuffers(scheme.getUsedBuffers());
        } catch (IllegalArgumentException e) {
            throw new ConnectionSchemeValidateException(scheme.getUid() != null ? scheme.getUid().toString() : "null", e.getMessage());
        }
    }

    private void validateUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID cannot be empty");
        }
        try {
            UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UID format");
        }
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
        try {
            UUID.fromString(clientUid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Client UID format");
        }
    }

    private void validateClientUid(UUID clientUid) {
        if (clientUid == null) {
            throw new IllegalArgumentException("Client UID cannot be null");
        }
    }

    private void validateSchemeJson(String schemeJson) {
        if (schemeJson == null || schemeJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheme JSON cannot be empty");
        }
        if (!schemeJson.trim().startsWith("{")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        
        // Проверяем структуру JSON
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(schemeJson, new TypeReference<Map<String, Object>>() {});
            
            // Проверяем наличие usedBuffers
            if (!jsonMap.containsKey("usedBuffers")) {
                throw new IllegalArgumentException("JSON must contain 'usedBuffers' field");
            }
            
            // Проверяем наличие bufferTransitions
            if (!jsonMap.containsKey("bufferTransitions")) {
                throw new IllegalArgumentException("JSON must contain 'bufferTransitions' field");
            }
            
            // Проверяем формат usedBuffers
            Object usedBuffersObj = jsonMap.get("usedBuffers");
            if (!(usedBuffersObj instanceof List)) {
                throw new IllegalArgumentException("'usedBuffers' must be an array");
            }
            
            // Проверяем формат bufferTransitions
            Object transitionsObj = jsonMap.get("bufferTransitions");
            if (!(transitionsObj instanceof Map)) {
                throw new IllegalArgumentException("'bufferTransitions' must be an object");
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON structure: " + e.getMessage());
        }
    }
    
    private void validateUsedBuffers(List<UUID> usedBuffers) {
        if (usedBuffers == null) {
            throw new IllegalArgumentException("Used buffers cannot be null");
        }
        // usedBuffers может быть пустым, но не null
    }
    
    private void validateBufferTransitions(Map<UUID, List<UUID>> bufferTransitions) {
        if (bufferTransitions == null) {
            throw new IllegalArgumentException("Buffer transitions cannot be null");
        }
        
        // Проверяем, что все ключи и значения - валидные UUID
        for (Map.Entry<UUID, List<UUID>> entry : bufferTransitions.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Buffer transition key cannot be null");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Buffer transition value list cannot be null");
            }
            for (UUID bufferUid : entry.getValue()) {
                if (bufferUid == null) {
                    throw new IllegalArgumentException("Buffer UID in transition list cannot be null");
                }
            }
        }
    }
}