package com.connection.scheme.events.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.scheme.events.ConnectionSchemeEventConstants;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetConnectionSchemesByClientUid extends Command {
     private final UUID clientUid;
    
    public GetConnectionSchemesByClientUid() {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEMES_BY_CLIENT_UID, "unknown", ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.clientUid = null;
    }
    
    public GetConnectionSchemesByClientUid(UUID ConnectionSchemeUid, String sourceService) {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEMES_BY_CLIENT_UID, sourceService, ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.clientUid = ConnectionSchemeUid;
    }
}