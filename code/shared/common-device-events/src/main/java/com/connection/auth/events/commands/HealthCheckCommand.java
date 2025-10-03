package com.connection.auth.events.commands;

import com.connection.auth.events.DeviceEventConstants;
import com.connection.common.events.Command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class HealthCheckCommand extends Command {
    
    public HealthCheckCommand() {
        super(DeviceEventConstants.COMMAND_HEALTH_CHECK, "unknown", DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
    }
    
    public HealthCheckCommand(String sourceService) {
        super(DeviceEventConstants.COMMAND_HEALTH_CHECK, sourceService, DeviceEventConstants.DEVICE_COMMANDS_TOPIC);
    }
}