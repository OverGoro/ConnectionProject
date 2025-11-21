package com.connection.scheme.mother;

import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.model.ConnectionSchemeDto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public class ConnectionSchemeObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID BUFFER_UID_1 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    private static final UUID BUFFER_UID_2 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174003");
    private static final UUID BUFFER_UID_3 =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174004");

    // ИСПРАВЛЕНО: scheme_json содержит только transitions
    private static final String DEFAULT_SCHEME_JSON =
            "{" + "\"" + BUFFER_UID_1 + "\":[\"" + BUFFER_UID_2 + "\"]," + "\""
                    + BUFFER_UID_2 + "\":[\"" + BUFFER_UID_3 + "\"]" + "}";

    // ИСПРАВЛЕНО: Пустой JSON для transitions
    private static final String EMPTY_TRANSITIONS_JSON = "{}";

    /** . */
    public static ConnectionSchemeDto createValidConnectionSchemeDto() {
        return ConnectionSchemeDto.builder().uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, 
                                BUFFER_UID_2, BUFFER_UID_3)) 
                .build();
    }

    /** . */
    public static ConnectionSchemeBlm createValidConnectionSchemeBlm() {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UID_1, Arrays.asList(BUFFER_UID_2));
        bufferTransitions.put(BUFFER_UID_2, Arrays.asList(BUFFER_UID_3));

        return ConnectionSchemeBlm.builder().uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3)) 
                .bufferTransitions(bufferTransitions).build();
    }

    /** . */
    public static ConnectionSchemeDalm createValidConnectionSchemeDalm() {
        return ConnectionSchemeDalm.builder().uid(DEFAULT_UID)
                .clientUid(DEFAULT_CLIENT_UID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3)) 
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createConnectionSchemeDtoWithNullUid() {
        return ConnectionSchemeDto.builder().uid(null)
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createConnectionSchemeDtoWithInvalidUid() {
        return ConnectionSchemeDto.builder().uid("invalid-uuid")
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createConnectionSchemeDtoWithEmptyJson() {
        return ConnectionSchemeDto.builder().uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString()).schemeJson("")
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createConnectionSchemeDtoWithInvalidJson() {
        return ConnectionSchemeDto.builder().uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson("invalid json")
                .usedBuffers(
                        Arrays.asList(BUFFER_UID_1, BUFFER_UID_2, BUFFER_UID_3))
                .build();
    }

    // ИСПРАВЛЕНО: Теперь это тест на пустые transitions
    /** . */
    public static ConnectionSchemeDto createConnectionSchemeDtoWithEmptyTransitions() {
        return ConnectionSchemeDto.builder().uid(DEFAULT_UID.toString())
                .clientUid(DEFAULT_CLIENT_UID.toString())
                .schemeJson(EMPTY_TRANSITIONS_JSON) // Пустые transitions
                .usedBuffers(Arrays.asList()) // Нет used buffers при пустых transitions
                .build();
    }

    /** . */
    public static ConnectionSchemeBlm createConnectionSchemeBlmWithNullFields() {
        return ConnectionSchemeBlm.builder().uid(null).clientUid(null)
                .schemeJson(null).usedBuffers(null).bufferTransitions(null)
                .build();
    }

    /** . */
    public static ConnectionSchemeDalm createConnectionSchemeForClient(
            UUID clientUid) {
        // ИСПРАВЛЕНО: scheme_json содержит только transitions
        String clientSpecificJson = "{" + "\"" + BUFFER_UID_1 + "\": []" 
            + "}";

        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(clientUid).schemeJson(clientSpecificJson)
                .usedBuffers(Arrays.asList(BUFFER_UID_1)) // Только ключевой буфер
                .build();
    }

    /** . */
    public static ConnectionSchemeDalm createConnectionSchemeDalmWithUsedBuffers(
            List<UUID> usedBuffers) {
        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(usedBuffers).build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с конкретными transitions
    /** . */
    public static ConnectionSchemeDalm createConnectionSchemeDalmWithTransitions(
            Map<UUID, List<UUID>> transitions) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            String schemeJson = objectMapper.writeValueAsString(transitions);

            // Вычисляем usedBuffers из transitions
            List<UUID> usedBuffers =
                    transitions.entrySet().stream().flatMap(entry -> {
                        java.util.stream.Stream<UUID> keyStream =
                                java.util.stream.Stream.of(entry.getKey());
                        java.util.stream.Stream<UUID> valueStream =
                                entry.getValue().stream();
                        return java.util.stream.Stream.concat(keyStream,
                                valueStream);
                    }).distinct().collect(java.util.stream.Collectors.toList());

            return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                    .clientUid(DEFAULT_CLIENT_UID).schemeJson(schemeJson)
                    .usedBuffers(usedBuffers).build();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create ConnectionSchemeDalm with transitions",
                    e);
        }
    }

    // ДОБАВЛЕНО: Метод для создания схемы без usedBuffers
    /** . */
    public static ConnectionSchemeDalm createConnectionSchemeDalmWithoutUsedBuffers() {
        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(null).build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с пустыми usedBuffers
    /** . */
    public static ConnectionSchemeDalm createConnectionSchemeDalmWithEmptyUsedBuffers() {
        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(DEFAULT_CLIENT_UID).schemeJson(DEFAULT_SCHEME_JSON)
                .usedBuffers(Arrays.asList()).build();
    }
}
