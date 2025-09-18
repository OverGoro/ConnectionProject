// BufferDALM.java
package com.connection.processing.buffer.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class BufferDALM {
    
    protected UUID uid;
    
    protected UUID connectionSchemeUid;
    
    protected Integer maxMessagesNumber;
    
    protected Integer maxMessageSize;
    
    protected String messagePrototype;
}