package com.connection.device.auth.events.domain;

import com.connection.common.events.BaseEvent;
import com.connection.device.auth.events.DeviceAuthEventConstants;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DeviceTokenCreatedEvent extends BaseEvent {
    private UUID deviceUid;
    private String email;
    private String username;

    /** . */

    public DeviceTokenCreatedEvent(UUID deviceUid, String email,
            String username) {
        super(DeviceAuthEventConstants.EVENT_DEVICE_TOKEN_CREATED,
                "device-auth-service");
        this.deviceUid = deviceUid;
        this.email = email;
        this.username = username;
    }
}
