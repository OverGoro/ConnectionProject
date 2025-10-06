// GetBuffersByConnectionSchemeUidCommand.java
package com.connection.buffer.events.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.common.events.Command;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetBuffersByConnectionSchemeUidCommand extends Command {
    private final UUID clientUid;
    private final UUID connectionSchemeUid;
    
    public GetBuffersByConnectionSchemeUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CONNECTION_SCHEME_UID, "unknown", BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.connectionSchemeUid = null;
        this.clientUid = null;
    }
    
    public GetBuffersByConnectionSchemeUidCommand(UUID clientUid, UUID connectionSchemeUid, String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CONNECTION_SCHEME_UID, sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.connectionSchemeUid = connectionSchemeUid;
        this.clientUid = clientUid;
    }
}