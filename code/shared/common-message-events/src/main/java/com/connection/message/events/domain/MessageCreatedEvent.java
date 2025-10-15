// MessageCreatedEvent.java
package com.connection.message.events.domain;

import java.util.Date;
import java.util.UUID;

import com.connection.common.events.BaseEvent;
import com.connection.message.events.MessageEventConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MessageCreatedEvent extends BaseEvent {
    private UUID messageUid;
    private UUID deviceUid;
    private UUID bufferUid;
    private String content;
    private String contentType;
    private Date createdAt;
    private UUID clientUid;

    public MessageCreatedEvent(
            UUID messageUid,
            UUID deviceUid,
            UUID bufferUid,
            String content,
            String contentType,
            Date createdAt,
            UUID clientUid) {
        super(MessageEventConstants.EVENT_MESSAGE_CREATED, "message-service");
        this.messageUid = messageUid;
        this.deviceUid = deviceUid;
        this.bufferUid = bufferUid;
        this.content = content;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.clientUid = clientUid;
    }
}