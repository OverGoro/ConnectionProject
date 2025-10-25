package com.connection.buffer.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.common.events.BaseEvent;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BufferCreatedEvent extends BaseEvent {
    private UUID bufferUid;
    
    public BufferCreatedEvent(UUID bufferUid) {
        super(BufferEventConstants.EVENT_BUFFER_CREATED, "buffer-service");
        this.bufferUid = bufferUid;
    }
}