package com.connection.device.auth.events.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import com.connection.common.events.Command;
import com.connection.device.auth.events.DeviceAuthEventConstants;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ValidateTokenCommand extends Command {
    private final String token;
    
    public ValidateTokenCommand() {
        super(DeviceAuthEventConstants.COMMAND_VALIDATE_TOKEN, "unknown", DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
        this.token = "unknown";
    }
    
    public ValidateTokenCommand(String token, String sourceService) {
        super(DeviceAuthEventConstants.COMMAND_VALIDATE_TOKEN, sourceService, DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
        this.token = token;
    }
}