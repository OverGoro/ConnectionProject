package com.service.connectionscheme.mother;

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

    /** . */
    public static final UUID CLIENT_UUID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    /** . */
    public static final UUID SCHEME_UUID =
            UUID.fromString("323e4567-e89b-12d3-a456-426614174003");
    /** . */
    public static final UUID BUFFER_UUID_1 =
            UUID.fromString("423e4567-e89b-12d3-a456-426614174004");
    /** . */
    public static final UUID BUFFER_UUID_2 =
            UUID.fromString("523e4567-e89b-12d3-a456-426614174005");
    /** . */
    public static final UUID BUFFER_UUID_3 =
            UUID.fromString("623e4567-e89b-12d3-a456-426614174006");

    /** . */
    public static final String VALID_TOKEN = "valid-token-123";
    /** . */
    public static final String INVALID_TOKEN = "invalid-token-456";

    // ИСПРАВЛЕНО: scheme_json содержит только transitions (без usedBuffers)
    /** . */
    public static final String SCHEME_JSON = "{" + "\"" + BUFFER_UUID_1
            + "\":[\"" + BUFFER_UUID_2 + "\"]," + "\"" + BUFFER_UUID_2
            + "\":[\"" + BUFFER_UUID_3 + "\"]" + "}";

    // ИСПРАВЛЕНО: Пустой JSON для transitions
    /** . */
    public static final String EMPTY_TRANSITIONS_JSON = "{}";

    /** . */
    public static ConnectionSchemeDto createValidSchemeDto() {
        return ConnectionSchemeDto.builder().uid(SCHEME_UUID.toString())
                .clientUid(CLIENT_UUID.toString()).schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2,
                        BUFFER_UUID_3)) // Все буферы из transitions
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createValidSchemeDto(UUID schemeUid,
            UUID clientUid, List<UUID> bufferUids) {
        if (bufferUids == null || bufferUids.isEmpty()) {
            return ConnectionSchemeDto.builder().uid(schemeUid.toString())
                    .clientUid(clientUid.toString()).schemeJson("{}")
                    .usedBuffers(List.of()).build();
        }

        // Создаем простой JSON с transitions
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (int i = 0; i < bufferUids.size(); i++) {
            if (i > 0) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\"").append(bufferUids.get(i)).append("\":[]");
        }
        jsonBuilder.append("}");

        return ConnectionSchemeDto.builder().uid(schemeUid.toString())
                .clientUid(clientUid.toString())
                .schemeJson(jsonBuilder.toString()).usedBuffers(bufferUids)
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createInvalidSchemeDto() {
        return ConnectionSchemeDto.builder().uid("invalid-uuid")
                .clientUid("invalid-client-uuid").schemeJson("") // empty json
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2)) // Добавлен usedBuffers
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createInvalidJsonStructureSchemeDto() {
        return ConnectionSchemeDto.builder().uid(SCHEME_UUID.toString())
                .clientUid(CLIENT_UUID.toString())
                .schemeJson("{\"invalid\": \"structure\"}") // missing required fields
                .usedBuffers(Arrays.asList(BUFFER_UUID_1)) // Добавлен usedBuffers
                .build();
    }

    /** . */
    public static ConnectionSchemeBlm createValidSchemeBlm() {
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UUID_1, Arrays.asList(BUFFER_UUID_2));
        bufferTransitions.put(BUFFER_UUID_2, Arrays.asList(BUFFER_UUID_3));

        return ConnectionSchemeBlm.builder().uid(SCHEME_UUID)
                .clientUid(CLIENT_UUID).schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2,
                        BUFFER_UUID_3)) // Все буферы из transitions
                .bufferTransitions(bufferTransitions).build();
    }

    /** . */
    public static ConnectionSchemeBlm createSchemeBlmWithEmptyTransitions() {
        return ConnectionSchemeBlm.builder().uid(SCHEME_UUID)
                .clientUid(CLIENT_UUID).schemeJson(EMPTY_TRANSITIONS_JSON) // Пустые transitions
                .usedBuffers(Arrays.asList()) // Нет used buffers при пустых transitions
                .bufferTransitions(new HashMap<>()).build();
    }

    /** . */
    public static ConnectionSchemeDalm createValidSchemeDalm() {
        return ConnectionSchemeDalm.builder().uid(SCHEME_UUID)
                .clientUid(CLIENT_UUID).schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2,
                        BUFFER_UUID_3)) // Все буферы из transitions
                .build();
    }

    /** . */
    public static ConnectionSchemeDalm createSchemeDalmWithUsedBuffers(
            List<UUID> usedBuffers) {
        return ConnectionSchemeDalm.builder().uid(SCHEME_UUID)
                .clientUid(CLIENT_UUID).schemeJson(SCHEME_JSON)
                .usedBuffers(usedBuffers).build();
    }

    /** . */
    public static ConnectionSchemeDto createSchemeDtoWithDifferentClient() {
        return ConnectionSchemeDto.builder().uid(SCHEME_UUID.toString())
                .clientUid(UUID.randomUUID().toString()) // different client
                .schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2,
                        BUFFER_UUID_3)) // Добавлен usedBuffers
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createSchemeDtoWithDifferentUid() {
        return ConnectionSchemeDto.builder().uid(UUID.randomUUID().toString()) // different UID
                .clientUid(CLIENT_UUID.toString()).schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2,
                        BUFFER_UUID_3)) // Добавлен usedBuffers
                .build();
    }

    /** . */
    public static ConnectionSchemeDto createSchemeDtoWithNullFields() {
        return ConnectionSchemeDto.builder().uid(null).clientUid(null)
                .schemeJson(null).usedBuffers(null) // Добавлен usedBuffers
                .build();
    }

    /** . */
    public static ConnectionSchemeBlm createSchemeBlmWithNullFields() {
        return ConnectionSchemeBlm.builder().uid(null).clientUid(null)
                .schemeJson(null).usedBuffers(null).bufferTransitions(null)
                .build();
    }

    /** . */
    public static ConnectionSchemeDalm createSchemeDalmWithNullFields() {
        return ConnectionSchemeDalm.builder().uid(null).clientUid(null)
                .schemeJson(null).usedBuffers(null).build();
    }

    // ИСПРАВЛЕНО: scheme_json содержит только transitions (без usedBuffers)
    /** . */
    public static ConnectionSchemeDto createSchemeDtoForBufferTest(
            UUID bufferUid) {
        String bufferSpecificJson = "{" + "\"" 
            + bufferUid + "\": []"
            + "}";

        return ConnectionSchemeDto.builder().uid(UUID.randomUUID().toString())
                .clientUid(CLIENT_UUID.toString())
                .schemeJson(bufferSpecificJson)
                .usedBuffers(Arrays.asList(bufferUid)) // Только указанный буфер
                .build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с пустыми transitions
    /** . */
    public static ConnectionSchemeDto createSchemeDtoWithEmptyTransitions() {
        return ConnectionSchemeDto.builder().uid(SCHEME_UUID.toString())
                .clientUid(CLIENT_UUID.toString())
                .schemeJson(EMPTY_TRANSITIONS_JSON) // Пустые transitions
                .usedBuffers(Arrays.asList()) // Нет used buffers при пустых transitions
                .build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с конкретными transitions
    /** . */
    public static ConnectionSchemeDalm createSchemeDalmWithTransitions(
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
                    .clientUid(CLIENT_UUID).schemeJson(schemeJson)
                    .usedBuffers(usedBuffers).build();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create ConnectionSchemeDalm with transitions",
                    e);
        }
    }

    // ДОБАВЛЕНО: Метод для создания схемы без usedBuffers
    /** . */
    public static ConnectionSchemeDalm createSchemeDalmWithoutUsedBuffers() {
        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(CLIENT_UUID).schemeJson(SCHEME_JSON)
                .usedBuffers(null).build();
    }

    // ДОБАВЛЕНО: Метод для создания схемы с пустыми usedBuffers
    /** . */
    public static ConnectionSchemeDalm createSchemeDalmWithEmptyUsedBuffers() {
        return ConnectionSchemeDalm.builder().uid(UUID.randomUUID())
                .clientUid(CLIENT_UUID).schemeJson(SCHEME_JSON)
                .usedBuffers(Arrays.asList()).build();
    }

    // ДОБАВЛЕНО: Метод для создания Blm с конкретными transitions
    /** . */
    public static ConnectionSchemeBlm createSchemeBlmWithTransitions(
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

            return ConnectionSchemeBlm.builder().uid(UUID.randomUUID())
                    .clientUid(CLIENT_UUID).schemeJson(schemeJson)
                    .usedBuffers(usedBuffers).bufferTransitions(transitions)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create ConnectionSchemeBlm with transitions", e);
        }
    }

    /** . */
    public static ConnectionSchemeDto createSchemeWithTransitions(
            UUID schemeUid, UUID clientUid, UUID fromBuffer, UUID toBuffer) {
        String schemeJson = "{\"" + fromBuffer + "\":[\"" + toBuffer + "\"]}";

        return ConnectionSchemeDto.builder().uid(schemeUid.toString())
                .clientUid(clientUid.toString()).schemeJson(schemeJson)
                .usedBuffers(Arrays.asList(fromBuffer, toBuffer)).build();
    }
}
