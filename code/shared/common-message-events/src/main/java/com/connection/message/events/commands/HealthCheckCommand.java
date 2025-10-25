// HealthCheckCommand.java
package com.connection.message.events.commands;

import com.connection.common.events.Command;
import com.connection.message.events.MessageEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class HealthCheckCommand extends Command {
    
    public HealthCheckCommand() {
        super(MessageEventConstants.COMMAND_HEALTH_CHECK, "unknown", MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
    }
    
    public HealthCheckCommand(String sourceService) {
        super(MessageEventConstants.COMMAND_HEALTH_CHECK, sourceService, MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
    }
}