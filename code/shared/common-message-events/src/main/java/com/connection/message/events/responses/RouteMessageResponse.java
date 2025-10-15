// RouteMessageResponse.java
package com.connection.message.events.responses;

import java.util.List;
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
public class RouteMessageResponse extends CommandResponse {
    private List<UUID> routedMessageUids;
    
    public static RouteMessageResponse success(String correlationId, List<UUID> routedMessageUids) {
        return RouteMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(true)
                .routedMessageUids(routedMessageUids)
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    public static RouteMessageResponse error(String correlationId, String error) {
        return RouteMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .success(false)
                .error(error)
                .timestamp(java.time.Instant.now())
                .build();
    }
}