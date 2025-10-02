package com.connection.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEvent {
    protected String eventId;
    protected String eventType;
    protected String sourceService;
    protected Instant timestamp;
    protected String correlationId;
    
    protected BaseEvent(String eventType, String sourceService) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }
}