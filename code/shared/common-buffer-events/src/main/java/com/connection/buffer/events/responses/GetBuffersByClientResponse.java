
package com.connection.buffer.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.processing.buffer.model.BufferDto;
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
public class GetBuffersByClientResponse extends CommandResponse {
    private List<BufferDto> bufferDtos;

    /** . */
    public static GetBuffersByClientResponse success(String correlationId,
            List<BufferDto> bufferDtos) {
        return GetBuffersByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true)
                .bufferDtos(bufferDtos).timestamp(java.time.Instant.now())
                .build();
    }

    /** . */
    public static GetBuffersByClientResponse error(String correlationId,
            String error) {
        return GetBuffersByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
