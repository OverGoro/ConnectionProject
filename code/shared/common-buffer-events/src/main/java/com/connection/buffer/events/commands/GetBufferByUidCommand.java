// GetBufferByUidCommand.java
package com.connection.buffer.events.commands;

import java.util.UUID;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.common.events.Command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetBufferByUidCommand extends Command {
    private final UUID bufferUid;
    private final UUID clientUid;
    
    public GetBufferByUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFER_BY_UID, "unknown", BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.bufferUid = null;
        this.clientUid = null;
    }
    
    public GetBufferByUidCommand(UUID clientUid, UUID bufferUid, String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFER_BY_UID, sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.bufferUid = bufferUid;
        this.clientUid = clientUid;
    }
}