
package com.connection.buffer.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.processing.buffer.model.BufferDto;
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
public class GetBufferByUidResponse extends CommandResponse {
    private BufferDto bufferDto;

    /** . */
    public static GetBufferByUidResponse success(String correlationId,
            BufferDto bufferDto) {
        return GetBufferByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).bufferDto(bufferDto)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetBufferByUidResponse error(String correlationId,
            String error) {
        return GetBufferByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
