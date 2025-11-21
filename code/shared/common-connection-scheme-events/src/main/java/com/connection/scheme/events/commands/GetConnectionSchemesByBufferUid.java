package com.connection.scheme.events.commands;

import com.connection.common.events.Command;
import com.connection.scheme.events.ConnectionSchemeEventConstants;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetConnectionSchemesByBufferUid extends Command {
    private final UUID bufferUid;

    /** . */
    public GetConnectionSchemesByBufferUid() {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEMES_BY_CLIENT_UID,
                "unknown",
                ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.bufferUid = null;
    }

    /** . */
    public GetConnectionSchemesByBufferUid(UUID bufferUid,
            String sourceService) {
        super(ConnectionSchemeEventConstants.COMMAND_GET_CONNECTION_SCHEMES_BY_CLIENT_UID,
                sourceService,
                ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
        this.bufferUid = bufferUid;
    }
}
