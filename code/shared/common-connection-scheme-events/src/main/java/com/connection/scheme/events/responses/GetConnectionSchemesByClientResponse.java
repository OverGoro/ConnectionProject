package com.connection.scheme.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.model.ConnectionSchemeDto;
import java.util.List;
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
public class GetConnectionSchemesByClientResponse extends CommandResponse {
    private List<ConnectionSchemeDto> connectionSchemeDtos;

    /** . */
    public static GetConnectionSchemesByClientResponse valid(
            String correlationId,
            List<ConnectionSchemeDto> connectionSchemeDtos) {
        return GetConnectionSchemesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true)
                .connectionSchemeDtos(connectionSchemeDtos)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetConnectionSchemesByClientResponse error(
            String correlationId, String error) {
        return GetConnectionSchemesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
