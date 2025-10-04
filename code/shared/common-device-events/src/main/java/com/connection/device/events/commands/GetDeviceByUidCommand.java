package com.connection.device.events.commands;

import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.device.events.DeviceEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetDeviceByUidCommand extends Command {
    private final UUID deviceUid;
    private final UUID clientUid;
    
    public GetDeviceByUidCommand() {
        super(DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID, "unknown", DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.deviceUid = null;
        this.clientUid = null;
    }
    
    public GetDeviceByUidCommand(UUID clientUid, UUID deviceUid, String sourceService) {
        super(DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID, sourceService, DeviceEventConstants.COMMAND_GET_DEVICE_BY_UID);
        this.deviceUid = deviceUid;
        this.clientUid = clientUid;
    }
}