package com.connection.scheme.events.domain;

import com.connection.common.events.BaseEvent;
import com.connection.scheme.events.ConnectionSchemeEventConstants;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ConnectionSchemeCreatedEvent extends BaseEvent {
    private UUID deviceUid;

    /** . */
    public ConnectionSchemeCreatedEvent(UUID deviceUid) {
        super(ConnectionSchemeEventConstants.EVENT_CONNECTION_SCHEME_CREATED,
                "connection-scheme-service");
        this.deviceUid = deviceUid;
    }
}
