package com.connection.scheme.events.commands;

import com.connection.common.events.Command;
import com.connection.scheme.events.ConnectionSchemeEventConstants;
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
        super(ConnectionSchemeEventConstants.COMMAND_HEALTH_CHECK, "unknown",
                ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
    }

    /** . */

    public HealthCheckCommand(String sourceService) {
        super(ConnectionSchemeEventConstants.COMMAND_HEALTH_CHECK,
                sourceService,
                ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
    }
}
