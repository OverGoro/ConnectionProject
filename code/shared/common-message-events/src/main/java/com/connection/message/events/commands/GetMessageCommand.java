// GetMessageCommand.java
package com.connection.message.events.commands;

import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.message.events.MessageEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetMessageCommand extends Command {
    private UUID deviceUuid;
    private boolean destroy;
    
    public GetMessageCommand() {
        super(MessageEventConstants.COMMAND_GET_MESSAGE, "unknown", MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
    }
    
    public GetMessageCommand(UUID deviceUuid, boolean destroy, String sourceService) {
        super(MessageEventConstants.COMMAND_GET_MESSAGE, sourceService, MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
        this.deviceUuid = deviceUuid;
        this.destroy = destroy;
    }
}