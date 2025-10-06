// GetBuffersByDeviceUidCommand.java
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
public class GetBuffersByDeviceUidCommand extends Command {
    private final UUID clientUid;
    private final UUID deviceUid;
    
    public GetBuffersByDeviceUidCommand() {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_DEVICE_UID, "unknown", BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.deviceUid = null;
        this.clientUid = null;
    }
    
    public GetBuffersByDeviceUidCommand(UUID clientUid, UUID deviceUid, String sourceService) {
        super(BufferEventConstants.COMMAND_GET_BUFFERS_BY_DEVICE_UID, sourceService, BufferEventConstants.BUFFER_RESPONSES_TOPIC);
        this.deviceUid = deviceUid;
        this.clientUid = clientUid;
    }
}