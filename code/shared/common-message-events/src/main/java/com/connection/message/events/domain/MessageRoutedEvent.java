// MessageRoutedEvent.java
package com.connection.message.events.domain;

import java.util.List;
import java.util.UUID;

import com.connection.common.events.BaseEvent;
import com.connection.message.events.MessageEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MessageRoutedEvent extends BaseEvent {
    private UUID sourceMessageUid;
    private UUID sourceBufferUid;
    private List<UUID> targetBufferUids;
    private List<UUID> createdMessageUids;
    private UUID deviceUid;
    
    public MessageRoutedEvent(UUID sourceMessageUid, UUID sourceBufferUid, 
                            List<UUID> targetBufferUids, List<UUID> createdMessageUids, UUID deviceUid) {
        super(MessageEventConstants.EVENT_MESSAGE_ROUTED, "message-service");
        this.sourceMessageUid = sourceMessageUid;
        this.sourceBufferUid = sourceBufferUid;
        this.targetBufferUids = targetBufferUids;
        this.createdMessageUids = createdMessageUids;
        this.deviceUid = deviceUid;
    }
}