
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
public class GetBuffersByDeviceUidCommand extends Command {
    private final UUID deviceUid;

    /** . */
    public GetBuffersByDeviceUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_DEVICE_UID, "unknown",
                BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.deviceUid = null;
    }

    /** . */
    public GetBuffersByDeviceUidCommand(UUID deviceUid, String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_DEVICE_UID,
                sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.deviceUid = deviceUid;
    }
}
