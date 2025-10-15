package com.connection.scheme.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.model.ConnectionSchemeDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetConnectionSchemesByBufferResponse extends CommandResponse {
    private List<ConnectionSchemeDTO> connectionSchemeDTOs;
    
    public static GetConnectionSchemesByBufferResponse valid(String correlationId, List<ConnectionSchemeDTO> connectionSchemeDTOs) {
        return GetConnectionSchemesByBufferResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .connectionSchemeDTOs(connectionSchemeDTOs)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetConnectionSchemesByBufferResponse error(String correlationId, String error) {
        return GetConnectionSchemesByBufferResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}