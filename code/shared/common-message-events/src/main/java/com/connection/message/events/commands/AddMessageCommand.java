// AddMessageCommand.java
package com.connection.message.events.commands;

import java.util.Date;
import java.util.UUID;

import com.connection.common.events.Command;
import com.connection.message.events.MessageEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AddMessageCommand extends Command {
    private UUID messageUid;
    private UUID deviceUid;
    private String content;
    private String contentType;
    private Date createdAt;
    
    public AddMessageCommand() {
        super(MessageEventConstants.COMMAND_ADD_MESSAGE, "unknown", MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
    }
    
    public AddMessageCommand(UUID deviceUid, String content, String contentType, String sourceService) {
        super(MessageEventConstants.COMMAND_ADD_MESSAGE, sourceService, MessageEventConstants.MESSAGE_RESPONSES_TOPIC);
        this.messageUid = UUID.randomUUID();
        this.deviceUid = deviceUid;
        this.content = content;
        this.contentType = contentType;
        this.createdAt = new Date();
    }
}