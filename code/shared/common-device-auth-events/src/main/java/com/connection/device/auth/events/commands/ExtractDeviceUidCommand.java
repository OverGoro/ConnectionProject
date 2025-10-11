package com.connection.device.auth.events.commands;

import com.connection.common.events.Command;
import com.connection.device.auth.events.DeviceAuthEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExtractDeviceUidCommand extends Command {
    private String token;
    private TokenType tokenType;
    
    public ExtractDeviceUidCommand() {
        super(DeviceAuthEventConstants.COMMAND_EXTRACT_DEVICE_UID, "unknown", DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
        this.token = "unknown";
        this.tokenType = TokenType.UNKNOWN;
    }
    
    public ExtractDeviceUidCommand(String token, String sourceService) {
        super(DeviceAuthEventConstants.COMMAND_EXTRACT_DEVICE_UID, sourceService, DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
        this.token = token;
        this.tokenType = TokenType.ACCESS;
    }
    
    public ExtractDeviceUidCommand(String token, TokenType tokenType, String sourceService) {
        super(DeviceAuthEventConstants.COMMAND_EXTRACT_DEVICE_UID, sourceService, DeviceAuthEventConstants.DEVICE_AUTH_RESPONSES_TOPIC);
        this.token = token;
        this.tokenType = tokenType;
    }
    
    public enum TokenType {
        ACCESS, REFRESH, UNKNOWN
    }
}