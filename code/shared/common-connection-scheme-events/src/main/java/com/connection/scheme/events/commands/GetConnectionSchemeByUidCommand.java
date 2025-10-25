package com.connection.scheme.events.commands;

import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.scheme.events.ConnectionSchemeEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetConnectionSchemeByUidCommand extends Command {
    private final UUID connectionSchemeUid;
    
    public GetConnectionSchemeByUidCommand() {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEME_BY_UID, "unknown", ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.connectionSchemeUid = null;
    }
    
    public GetConnectionSchemeByUidCommand(UUID ConnectionSchemeUid, String sourceService) {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEME_BY_UID, sourceService, ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.connectionSchemeUid = ConnectionSchemeUid;
    }
}