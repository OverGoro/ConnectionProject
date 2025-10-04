package com.connection.device.events.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

import com.connection.common.events.CommandResponse;
import com.connection.device.model.DeviceDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetDevicesByClientResponse extends CommandResponse {
    private List<DeviceDTO> deviceDTOs;
    
    public static GetDevicesByClientResponse valid(String correlationId, List<DeviceDTO> deviceDTOs) {
        return GetDevicesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .deviceDTOs(deviceDTOs)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static GetDevicesByClientResponse error(String correlationId, String error) {
        return GetDevicesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}