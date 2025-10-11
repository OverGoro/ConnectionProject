package com.connection.device.auth.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.CommandResponse;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeviceUidResponse extends CommandResponse {
    private UUID deviceUid;
    private String tokenType;
    
    public static DeviceUidResponse success(String correlationId, UUID deviceUid, String tokenType) {
        return DeviceUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .deviceUid(deviceUid)
                .tokenType(tokenType)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static DeviceUidResponse error(String correlationId, String error) {
        return DeviceUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}