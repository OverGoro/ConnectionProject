package com.connection.auth.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.auth.events.DeviceEventConstants;
import com.connection.common.events.BaseEvent;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DeviceCreatedEvent extends BaseEvent {
    private UUID deviceUid;
    
    public DeviceCreatedEvent(UUID deviceUid) {
        super(DeviceEventConstants.EVENT_DEVICE_CREATED, "device-service");
        this.deviceUid = deviceUid;
    }
}