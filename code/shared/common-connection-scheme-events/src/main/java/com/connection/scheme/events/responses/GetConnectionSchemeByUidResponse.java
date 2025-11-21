package com.connection.scheme.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.model.ConnectionSchemeDto;
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
public class GetConnectionSchemeByUidResponse extends CommandResponse {
    private ConnectionSchemeDto connectionSchemeDto;

    /** . */
    public static GetConnectionSchemeByUidResponse success(String correlationId,
            ConnectionSchemeDto connectionSchemeDto) {
        return GetConnectionSchemeByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true)
                .connectionSchemeDto(connectionSchemeDto)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetConnectionSchemeByUidResponse error(String correlationId,
            String error) {
        return GetConnectionSchemeByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
