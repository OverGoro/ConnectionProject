package com.connection.buffer.events.commands;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.common.events.Command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class HealthCheckCommand extends Command {
    
    public HealthCheckCommand() {
        super(BufferEventConstants.COMMAND_HEALTH_CHECK, "unknown", BufferEventConstants.BUFFER_COMMANDS_TOPIC);
    }
    
    public HealthCheckCommand(String sourceService) {
        super(BufferEventConstants.COMMAND_HEALTH_CHECK, sourceService, BufferEventConstants.BUFFER_COMMANDS_TOPIC);
    }
}