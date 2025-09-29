// ConnectionSchemeConverter.java
package com.connection.scheme.converter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeConverter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public ConnectionSchemeBLM toBLM(ConnectionSchemeDALM dalm) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions = extractTransitionsFromJson(dalm.getSchemeJson());
            
            return ConnectionSchemeBLM.builder()
                .uid(dalm.getUid())
                .clientUid(dalm.getClientUid())
                .schemeJson(dalm.getSchemeJson())
                .usedBuffers(dalm.getUsedBuffers())
                .bufferTransitions(bufferTransitions)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DALM to BLM", e);
        }
    }

    public ConnectionSchemeBLM toBLM(ConnectionSchemeDTO dto) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions = extractTransitionsFromJson(dto.getSchemeJson());
            
            return ConnectionSchemeBLM.builder()
                .uid(UUID.fromString(dto.getUid()))
                .clientUid(UUID.fromString(dto.getClientUid()))
                .schemeJson(dto.getSchemeJson())
                .usedBuffers(extractUsedBuffersFromJson(dto.getSchemeJson()))
                .bufferTransitions(bufferTransitions)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to BLM", e);
        }
    }

    public ConnectionSchemeDTO toDTO(ConnectionSchemeBLM blm) {
        return ConnectionSchemeDTO.builder()
            .uid(blm.getUid().toString())
            .clientUid(blm.getClientUid().toString())
            .schemeJson(blm.getSchemeJson())
            .build();
    }

    public ConnectionSchemeDALM toDALM(ConnectionSchemeBLM blm) {
        return ConnectionSchemeDALM.builder()
            .uid(blm.getUid())
            .clientUid(blm.getClientUid())
            .schemeJson(blm.getSchemeJson())
            .usedBuffers(blm.getUsedBuffers())
            .build();
    }
    
    private Map<UUID, List<UUID>> extractTransitionsFromJson(String schemeJson) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(schemeJson, new TypeReference<Map<String, Object>>() {});
            
            @SuppressWarnings("unchecked")
            Map<String, List<String>> transitionsMap = (Map<String, List<String>>) jsonMap.get("bufferTransitions");
            
            return objectMapper.convertValue(transitionsMap, new TypeReference<Map<UUID, List<UUID>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract transitions from JSON", e);
        }
    }
    
    private List<UUID> extractUsedBuffersFromJson(String schemeJson) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(schemeJson, new TypeReference<Map<String, Object>>() {});
            
            @SuppressWarnings("unchecked")
            List<String> usedBuffersList = (List<String>) jsonMap.get("usedBuffers");
            
            return objectMapper.convertValue(usedBuffersList, new TypeReference<List<UUID>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract used buffers from JSON", e);
        }
    }
}