package com.connection.device.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.device.model.DeviceDto;
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
public class GetDevicesByClientResponse extends CommandResponse {
    private List<DeviceDto> deviceDtos;

    /** . */
    public static GetDevicesByClientResponse valid(String correlationId,
            List<DeviceDto> deviceDtos) {
        return GetDevicesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true)
                .deviceDtos(deviceDtos).timestamp(java.time.Instant.now())
                .build();
    }

    /** . */
    public static GetDevicesByClientResponse error(String correlationId,
            String error) {
        return GetDevicesByClientResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
