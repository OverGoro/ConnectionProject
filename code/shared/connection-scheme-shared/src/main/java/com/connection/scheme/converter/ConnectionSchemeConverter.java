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

    private  final ObjectMapper objectMapper = new ObjectMapper();

    public  ConnectionSchemeBLM toBLM(ConnectionSchemeDALM dalm) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions = extractTransitionsFromJson(dalm.getSchemeJson());

            return ConnectionSchemeBLM.builder()
                    .uid(dalm.getUid())
                    .clientUid(dalm.getClientUid())
                    .schemeJson(dalm.getSchemeJson())
                    .usedBuffers(dalm.getUsedBuffers()) // Теперь получаем из DALM
                    .bufferTransitions(bufferTransitions)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DALM to BLM", e);
        }
    }

    public  ConnectionSchemeBLM toBLM(ConnectionSchemeDTO dto) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions = extractTransitionsFromJson(dto.getSchemeJson());

            return ConnectionSchemeBLM.builder()
                    .uid(UUID.fromString(dto.getUid()))
                    .clientUid(UUID.fromString(dto.getClientUid()))
                    .schemeJson(dto.getSchemeJson())
                    .usedBuffers(dto.getUsedBuffers()) // Используем переданные usedBuffers
                    .bufferTransitions(bufferTransitions)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to BLM", e);
        }
    }

    public  ConnectionSchemeDTO toDTO(ConnectionSchemeBLM blm) {

        String transitionsJson = convertTransitionsToJson(blm.getBufferTransitions());

        return ConnectionSchemeDTO.builder()
                .uid(blm.getUid().toString())
                .clientUid(blm.getClientUid().toString())
                .schemeJson(transitionsJson)
                .usedBuffers(blm.getUsedBuffers())
                .build();
    }

    public  ConnectionSchemeDALM toDALM(ConnectionSchemeBLM blm) {

        String transitionsJson = convertTransitionsToJson(blm.getBufferTransitions());

        return ConnectionSchemeDALM.builder()
                .uid(blm.getUid())
                .clientUid(blm.getClientUid())
                .schemeJson(transitionsJson)
                .usedBuffers(blm.getUsedBuffers())
                .build();
    }

    private  Map<UUID, List<UUID>> extractTransitionsFromJson(String schemeJson) {
        try {

            return objectMapper.readValue(schemeJson, new TypeReference<Map<UUID, List<UUID>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract transitions from JSON", e);
        }
    }

    private  List<UUID> extractUsedBuffersFromJson(String schemeJson) {
        try {

            Map<UUID, List<UUID>> transitions = extractTransitionsFromJson(schemeJson);

            return transitions.entrySet().stream()
                    .flatMap(entry -> {
                        java.util.stream.Stream<UUID> keyStream = java.util.stream.Stream.of(entry.getKey());
                        java.util.stream.Stream<UUID> valueStream = entry.getValue().stream();
                        return java.util.stream.Stream.concat(keyStream, valueStream);
                    })
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract used buffers from JSON", e);
        }
    }

    private  String convertTransitionsToJson(Map<UUID, List<UUID>> bufferTransitions) {
        try {
            return objectMapper.writeValueAsString(bufferTransitions);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert transitions to JSON", e);
        }
    }
}