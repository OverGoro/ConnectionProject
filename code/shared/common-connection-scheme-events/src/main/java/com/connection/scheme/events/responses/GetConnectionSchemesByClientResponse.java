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
public class GetConnectionSchemesByClientResponse extends CommandResponse {
    private List<ConnectionSchemeDTO> connectionSchemeDTOs;
    
    public static GetConnectionSchemesByClientResponse valid(String correlationId, List<ConnectionSchemeDTO> connectionSchemeDTOs) {
        return GetConnectionSchemesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .connectionSchemeDTOs(connectionSchemeDTOs)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetConnectionSchemesByClientResponse error(String correlationId, String error) {
        return GetConnectionSchemesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}