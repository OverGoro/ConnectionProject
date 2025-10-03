package com.connection.auth.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.CommandResponse;
import com.connection.device.model.DeviceDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetDeviceByUidResponse extends CommandResponse {
    private DeviceDTO deviceDTO;
    
    public static GetDeviceByUidResponse success(String correlationId, DeviceDTO deviceDTO) {
        return GetDeviceByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .deviceDTO(deviceDTO)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetDeviceByUidResponse error(String correlationId, String error) {
        return GetDeviceByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}