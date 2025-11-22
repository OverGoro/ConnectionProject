
package com.connection.buffer.events.commands;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.common.events.Command;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetBuffersByClientUidCommand extends Command {
    private final UUID clientUid;

    /** . */
    public GetBuffersByClientUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CLIENT_UID, "unknown",
                BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.clientUid = null;
    }

    /** . */
    public GetBuffersByClientUidCommand(UUID clientUid, String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CLIENT_UID,
                sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.clientUid = clientUid;
    }
}
