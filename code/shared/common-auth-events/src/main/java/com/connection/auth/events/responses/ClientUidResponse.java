package com.connection.auth.events.responses;

import com.connection.common.events.CommandResponse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClientUidResponse extends CommandResponse {
    private UUID clientUid;
    private String tokenType;

    /** . */
    public static ClientUidResponse success(String correlationId,
            UUID clientUid, String tokenType) {
        return ClientUidResponse.builder().eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).clientUid(clientUid)
                .tokenType(tokenType).timestamp(java.time.Instant.now())
                .build();
    }

    /** . */
    public static ClientUidResponse error(String correlationId, String error) {
        return ClientUidResponse.builder().eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
