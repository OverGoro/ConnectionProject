
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
public class GetBuffersByConnectionSchemeUidCommand extends Command {
    private final UUID connectionSchemeUid;

    /** . */
    public GetBuffersByConnectionSchemeUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CONNECTION_SCHEME_UID,
                "unknown", BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.connectionSchemeUid = null;
    }

    /** . */
    public GetBuffersByConnectionSchemeUidCommand(UUID connectionSchemeUid,
            String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_CONNECTION_SCHEME_UID,
                sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.connectionSchemeUid = connectionSchemeUid;
    }
}
