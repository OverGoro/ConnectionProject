package com.connection.processing.buffer.objects.json.mother;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

public class BufferJsonDataObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_BUFFER_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_DATA = "{\"key\": \"value\", \"number\": 42}";
    private static final Instant DEFAULT_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    public static BufferJsonDataDTO createValidBufferJsonDataDTO() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(DEFAULT_DATA)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataBLM createValidBufferJsonDataBLM() {
        return BufferJsonDataBLM.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .data(DEFAULT_DATA)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDALM createValidBufferJsonDataDALM() {
        return BufferJsonDataDALM.builder()
                .uid(DEFAULT_UID)
                .bufferUid(DEFAULT_BUFFER_UID)
                .data(DEFAULT_DATA)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithNullUid() {
        return BufferJsonDataDTO.builder()
                .uid(null)
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(DEFAULT_DATA)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithInvalidUid() {
        return BufferJsonDataDTO.builder()
                .uid("invalid-uuid")
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(DEFAULT_DATA)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithEmptyData() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data("")
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithNullData() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(null)
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataBLM createBufferJsonDataBLMWithNullFields() {
        return BufferJsonDataBLM.builder()
                .uid(null)
                .bufferUid(null)
                .data(null)
                .createdAt(null)
                .build();
    }

    public static BufferJsonDataDALM createBufferJsonDataForBuffer(UUID bufferUid) {
        return BufferJsonDataDALM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(bufferUid)
                .data("{\"buffer\": \"" + bufferUid + "\", \"timestamp\": \"" + Instant.now() + "\"}")
                .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();
    }

    public static BufferJsonDataDALM createBufferJsonDataWithTimestamp(UUID bufferUid, Instant timestamp) {
        return BufferJsonDataDALM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(bufferUid)
                .data("{\"timestamp\": \"" + timestamp + "\"}")
                .createdAt(timestamp)
                .build();
    }
    // Добавляем в существующий BufferJsonDataObjectMother.java

    public static BufferJsonDataDTO createBufferJsonDataDTOWithInvalidJson() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data("invalid json")
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithArrayJson() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data("[1, 2, 3]")
                .createdAt(DEFAULT_CREATED_AT)
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithFutureTimestamp() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(DEFAULT_DATA)
                .createdAt(Instant.now().plusSeconds(3600))
                .build();
    }

    public static BufferJsonDataDTO createBufferJsonDataDTOWithNullTimestamp() {
        return BufferJsonDataDTO.builder()
                .uid(DEFAULT_UID.toString())
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .data(DEFAULT_DATA)
                .createdAt(null)
                .build();
    }
}