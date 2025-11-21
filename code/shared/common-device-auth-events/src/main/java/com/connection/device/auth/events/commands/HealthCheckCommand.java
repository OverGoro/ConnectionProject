package com.connection.device.auth.events.commands;

import com.connection.common.events.Command;
import com.connection.device.auth.events.DeviceAuthEventConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class HealthCheckCommand extends Command {
    /** . */
    public HealthCheckCommand() {
        super(DeviceAuthEventConstants.COMMAND_HEALTH_CHECK, "unknown",
                DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
    }

    /** . */
    public HealthCheckCommand(String sourceService) {
        super(DeviceAuthEventConstants.COMMAND_HEALTH_CHECK, sourceService,
                DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
    }
}
