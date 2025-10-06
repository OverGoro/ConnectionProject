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
        
        // Проверяем структуру JSON (теперь только transitions)
        try {
            Map<UUID, List<UUID>> transitions = objectMapper.readValue(
                schemeJson, 
                new TypeReference<Map<UUID, List<UUID>>>() {}
            );
            
            // Проверяем, что transitions не пусты
            if (transitions.isEmpty()) {
                throw new IllegalArgumentException("Transitions cannot be empty");
            }
            
            // Проверяем формат UUID в ключах и значениях
            for (Map.Entry<UUID, List<UUID>> entry : transitions.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("Transition key cannot be null");
                }
                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("Transition value list cannot be null");
                }
                for (UUID bufferUid : entry.getValue()) {
                    if (bufferUid == null) {
                        throw new IllegalArgumentException("Buffer UID in transition list cannot be null");
                    }
                }
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid transitions JSON structure: " + e.getMessage());
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