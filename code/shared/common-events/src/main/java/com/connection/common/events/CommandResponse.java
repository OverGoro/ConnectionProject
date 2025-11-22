package com.connection.common.events;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CommandResponse extends BaseEvent {
    private boolean success;
    private Object data;
    private String error;

    /** . */
    public static CommandResponse success(String correlationId, Object data) {
        return CommandResponse.builder().eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).data(data)
                .timestamp(Instant.now()).build();
    }

    /** . */
    public static CommandResponse error(String correlationId, String error) {
        return CommandResponse.builder().eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(Instant.now()).build();
    }
}
