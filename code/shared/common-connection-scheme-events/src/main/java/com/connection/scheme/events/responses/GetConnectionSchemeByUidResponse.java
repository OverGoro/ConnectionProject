package com.connection.scheme.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.model.ConnectionSchemeDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetConnectionSchemeByUidResponse extends CommandResponse {
    private ConnectionSchemeDTO connectionSchemeDTO;
    
    public static GetConnectionSchemeByUidResponse success(String correlationId, ConnectionSchemeDTO connectionSchemeDTO) {
        return GetConnectionSchemeByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .connectionSchemeDTO(connectionSchemeDTO)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetConnectionSchemeByUidResponse error(String correlationId, String error) {
        return GetConnectionSchemeByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}