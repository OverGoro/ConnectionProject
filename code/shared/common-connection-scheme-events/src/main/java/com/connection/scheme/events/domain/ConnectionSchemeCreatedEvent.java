package com.connection.scheme.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.BaseEvent;
import com.connection.scheme.events.ConnectionSchemeEventConstants;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ConnectionSchemeCreatedEvent extends BaseEvent {
    private UUID deviceUid;
    
    public ConnectionSchemeCreatedEvent(UUID deviceUid) {
        super(ConnectionSchemeEventConstants.EVENT_CONNECTION_SCHEME_CREATED, "connection-scheme-service");
        this.deviceUid = deviceUid;
    }
}