// GetMessagesByBufferCommand.java
package com.connection.message.events.commands;

import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.message.events.MessageEventConstants;
import com.connection.message.model.MessageDirection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GetMessagesByBufferCommand extends Command {
    private UUID bufferUid;
    
    public GetMessagesByBufferCommand() {
        super(MessageEventConstants.COMMAND_GET_MESSAGES_BY_BUFFER, "unknown", MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
    }
    
    public GetMessagesByBufferCommand(UUID bufferUid, MessageDirection direction, String sourceService) {
        super(MessageEventConstants.COMMAND_GET_MESSAGES_BY_BUFFER, sourceService, MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
        this.bufferUid = bufferUid;
    }
}