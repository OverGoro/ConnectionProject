package com.connection.scheme.converter;

import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.model.ConnectionSchemeDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public class ConnectionSchemeConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** . */
    public ConnectionSchemeBlm toBlm(ConnectionSchemeDalm dalm) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions =
                    extractTransitionsFromJson(dalm.getSchemeJson());

            return ConnectionSchemeBlm.builder().uid(dalm.getUid())
                    .clientUid(dalm.getClientUid())
                    .schemeJson(dalm.getSchemeJson())
                    .usedBuffers(dalm.getUsedBuffers()) // Теперь получаем из Dalm
                    .bufferTransitions(bufferTransitions).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Dalm to Blm", e);
        }
    }

    /** . */
    public ConnectionSchemeBlm toBlm(ConnectionSchemeDto dto) {
        try {
            // Извлекаем transitions из JSON
            Map<UUID, List<UUID>> bufferTransitions =
                    extractTransitionsFromJson(dto.getSchemeJson());

            return ConnectionSchemeBlm.builder()
                    .uid(UUID.fromString(dto.getUid()))
                    .clientUid(UUID.fromString(dto.getClientUid()))
                    .schemeJson(dto.getSchemeJson())
                    .usedBuffers(dto.getUsedBuffers()) // Используем переданные usedBuffers
                    .bufferTransitions(bufferTransitions).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Dto to Blm", e);
        }
    }

    /** . */
    public ConnectionSchemeDto toDto(ConnectionSchemeBlm blm) {

        String transitionsJson =
                convertTransitionsToJson(blm.getBufferTransitions());

        return ConnectionSchemeDto.builder().uid(blm.getUid().toString())
                .clientUid(blm.getClientUid().toString())
                .schemeJson(transitionsJson).usedBuffers(blm.getUsedBuffers())
                .build();
    }

    /** . */
    public ConnectionSchemeDalm toDalm(ConnectionSchemeBlm blm) {

        String transitionsJson =
                convertTransitionsToJson(blm.getBufferTransitions());

        return ConnectionSchemeDalm.builder().uid(blm.getUid())
                .clientUid(blm.getClientUid()).schemeJson(transitionsJson)
                .usedBuffers(blm.getUsedBuffers()).build();
    }

    private Map<UUID, List<UUID>> extractTransitionsFromJson(
            String schemeJson) {
        try {

            return objectMapper.readValue(schemeJson,
                    new TypeReference<Map<UUID, List<UUID>>>() {});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract transitions from JSON", e);
        }
    }

    private List<UUID> extractUsedBuffersFromJson(String schemeJson) {
        try {

            Map<UUID, List<UUID>> transitions =
                    extractTransitionsFromJson(schemeJson);

            return transitions.entrySet().stream().flatMap(entry -> {
                java.util.stream.Stream<UUID> keyStream =
                        java.util.stream.Stream.of(entry.getKey());
                java.util.stream.Stream<UUID> valueStream =
                        entry.getValue().stream();
                return java.util.stream.Stream.concat(keyStream, valueStream);
            }).distinct().collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract used buffers from JSON", e);
        }
    }

    private String convertTransitionsToJson(
            Map<UUID, List<UUID>> bufferTransitions) {
        try {
            return objectMapper.writeValueAsString(bufferTransitions);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert transitions to JSON",
                    e);
        }
    }
}
