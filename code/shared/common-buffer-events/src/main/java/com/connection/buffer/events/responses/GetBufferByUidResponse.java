// GetBufferByUidResponse.java
package com.connection.buffer.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.CommandResponse;
import com.connection.processing.buffer.model.BufferDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetBufferByUidResponse extends CommandResponse {
    private BufferDTO bufferDTO;
    
    public static GetBufferByUidResponse success(String correlationId, BufferDTO bufferDTO) {
        return GetBufferByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .bufferDTO(bufferDTO)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetBufferByUidResponse error(String correlationId, String error) {
        return GetBufferByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}