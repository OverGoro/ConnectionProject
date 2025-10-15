// HealthCheckResponse.java
package com.connection.message.events.responses;

import java.util.Map;
import java.util.UUID;

import com.connection.common.events.CommandResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HealthCheckResponse extends CommandResponse {
    private Map<String, Object> healthStatus;
    
    public static HealthCheckResponse success(String correlationId, Map<String, Object> healthStatus) {
        return HealthCheckResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .healthStatus(healthStatus)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static HealthCheckResponse error(String correlationId, String error) {
        return HealthCheckResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}