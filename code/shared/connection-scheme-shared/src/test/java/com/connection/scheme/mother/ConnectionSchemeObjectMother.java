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

    // ИСПРАВЛЕНО: scheme_json содержит только transitions
    private static final String DEFAULT_SCHEME_JSON = "{" +
            "\"" + BUFFER_UID_1 + "\":[\"" + BUFFER_UID_2 + "\"]," +
            "\"" + BUFFER_UID_2 + "\":[\"" + BUFFER_UID_3 + "\"]" +
            "}";

    // ИСПРАВЛЕНО: Пустой JSON для transitions
    private static final String EMPTY_TRANSITIONS_JSON = "{}";

    public static ConnectionSchemeDTO createValidConnectionSchemeDTO() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3)) // Все буферы из transitions
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
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3)) // Все буферы из transitions
                .bufferTransitions(bufferTransitions)
                .build();
    }

    public static ConnectionSchemeDALM createValidConnectionSchemeDALM() {
        return ConnectionSchemeDALM.builder()
                .uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3)) // Все буферы из transitions
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithNullUid() {
        return ConnectionSchemeDTO.builder()
                .uid(null)
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithInvalidUid() {
        return ConnectionSchemeDTO.builder()
                .uid("invalid-uuid")
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithEmptyJson() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("")
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    public static ConnectionSchemeDTO createConnectionSchemeDTOWithInvalidJson() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("invalid json")
                .usedBuffers(Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    // ИСПРАВЛЕНО: Теперь это тест на пустые transitions
    public static ConnectionSchemeDTO createConnectionSchemeDTOWithEmptyTransitions() {
        return ConnectionSchemeDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(EMPTY_TRANSITIONS_JSON) // Пустые transitions
                .usedBuffers(Arrays.asList()) // Нет used buffers при пустых transitions
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
        // ИСПРАВЛЕНО: scheme_json содержит только transitions
        String clientSpecificJson = "{" +
                "\"" + BUFFER_UID_1 + "\": []" + // Пустой список transitions
                "}";

        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(clientUid)
                .schemeJson(clientSpecificJson)
                .usedBuffers(Arrays.asList(BUFFER_UID_1)) // Только ключевой буфер
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

    // ДОБАВЛЕНО: Метод для создания схемы с конкретными transitions
    public static ConnectionSchemeDALM createConnectionSchemeDALMWithTransitions(Map<UUID, List<UUID>> transitions) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String schemeJson = objectMapper.writeValueAsString(transitions);
            
            // Вычисляем usedBuffers из transitions
            List<UUID> usedBuffers = transitions.entrySet().stream()
                    .flatMap(entry -> {
                        java.util.stream.Stream<UUID> keyStream = java.util.stream.Stream.of(entry.getKey());
                        java.util.stream.Stream<UUID> valueStream = entry.getValue().stream();
                        return java.util.stream.Stream.concat(keyStream, valueStream);
                    })
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            return ConnectionSchemeDALM.builder()
                    .uid(UUID.randomUUID())
                    .clientUid(DEFAULT_CLIENT_UID)
                    .schemeJson(schemeJson)
                    .usedBuffers(usedBuffers)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ConnectionSchemeDALM with transitions", e);
        }
    }

    // ДОБАВЛЕНО: Метод для создания схемы без usedBuffers
    public static ConnectionSchemeDALM createConnectionSchemeDALMWithoutUsedBuffers() {
        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(null)
                .build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с пустыми usedBuffers
    public static ConnectionSchemeDALM createConnectionSchemeDALMWithEmptyUsedBuffers() {
        return ConnectionSchemeDALM.builder()
                .uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID)
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList())
                .build();
    }
}