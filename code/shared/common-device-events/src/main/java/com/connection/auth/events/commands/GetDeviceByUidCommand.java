package com.connection.auth.events.commands;

import java.util.UUID;

import com.connection.common.events.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import com.connection.auth.events.DeviceEventConstants;
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetDeviceByUidCommand extends Command {
    private final UUID deviceUid;
    
    public GetDeviceByUidCommand() {
        super(DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID, "unknown", DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.deviceUid = null;
    }
    
    public GetDeviceByUidCommand(UUID deviceUid, String sourceService) {
        super(DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID, sourceService, DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID);
        this.deviceUid = deviceUid;
    }
}