package com.connection.device.auth.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.BaseEvent;
import com.connection.device.auth.events.DeviceAuthEventConstants;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TokenValidatedEvent extends BaseEvent {
    private UUID deviceUid;
    private boolean isValid;
    private String tokenType;
    
    public TokenValidatedEvent(UUID deviceUid, boolean isValid, String tokenType) {
        super(DeviceAuthEventConstants.EVENT_DEVICE_TOKEN_VALIDATED, "device-auth-service");
        this.deviceUid = deviceUid;
        this.isValid = isValid;
        this.tokenType = tokenType;
    }
}