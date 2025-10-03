package com.connection.auth.events.commands;

import com.connection.common.events.Command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class HealthCheckCommand extends Command {
    
    public HealthCheckCommand() {
        super("HEALTH_CHECK", "unknown", "auth.responses");
    }
    
    public HealthCheckCommand(String sourceService) {
        super("HEALTH_CHECK", sourceService, "auth.responses");
    }
}