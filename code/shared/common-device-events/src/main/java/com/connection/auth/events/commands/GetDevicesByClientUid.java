package com.connection.auth.events.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.auth.events.DeviceEventConstants;
import com.connection.common.events.Command;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetDevicesByClientUid extends Command {
     private final UUID clientUid;
    
    public GetDevicesByClientUid() {
        super(DeviceEventConstants.COMMAND_GET_DEVICES_BY_CLIENT_UID, "unknown", DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.clientUid = null;
    }
    
    public GetDevicesByClientUid(UUID deviceUid, String sourceService) {
        super(DeviceEventConstants.COMMAND_GET_DEVICES_BY_CLIENT_UID, sourceService, DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.clientUid = deviceUid;
    }
}