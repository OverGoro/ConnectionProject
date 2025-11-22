package com.connection.device.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.device.model.DeviceDto;
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
public class GetDeviceByUidResponse extends CommandResponse {
    private DeviceDto deviceDto;

    /** . */
    public static GetDeviceByUidResponse success(String correlationId,
            DeviceDto deviceDto) {
        return GetDeviceByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).deviceDto(deviceDto)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetDeviceByUidResponse error(String correlationId,
            String error) {
        return GetDeviceByUidResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
