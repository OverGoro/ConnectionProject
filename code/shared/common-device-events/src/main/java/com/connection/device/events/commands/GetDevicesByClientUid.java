package com.connection.device.events.commands;

import com.connection.common.events.Command;
import com.connection.device.events.DeviceEventConstants;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetDevicesByClientUid extends Command {
    private final UUID clientUid;

    /** . */
    public GetDevicesByClientUid() {
        super(DeviceEventConstants.COMMAND_GET_DEVICES_BY_CLIENT_UID, "unknown",
                DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.clientUid = null;
    }

    /** . */
    public GetDevicesByClientUid(UUID deviceUid, String sourceService) {
        super(DeviceEventConstants.COMMAND_GET_DEVICES_BY_CLIENT_UID,
                sourceService, DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
        this.clientUid = deviceUid;
    }
}
