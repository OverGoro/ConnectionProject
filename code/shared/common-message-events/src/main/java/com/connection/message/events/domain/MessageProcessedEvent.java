
package com.connection.message.events.domain;

import com.connection.common.events.BaseEvent;
import com.connection.message.events.MessageEventConstants;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MessageProcessedEvent extends BaseEvent {
    private UUID messageUid;
    private UUID bufferUid;
    private String status;
    private UUID deviceUid;

    /** . */
    public MessageProcessedEvent(UUID messageUid, UUID bufferUid, String status,
            UUID deviceUid) {
        super(MessageEventConstants.EVENT_MESSAGE_PROCESSED, "message-service");
        this.messageUid = messageUid;
        this.bufferUid = bufferUid;
        this.status = status;
        this.deviceUid = deviceUid;
    }
}
