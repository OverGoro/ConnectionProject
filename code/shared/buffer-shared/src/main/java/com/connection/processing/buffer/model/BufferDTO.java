// BufferDTO.java
package com.connection.processing.buffer.model;

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
public class BufferDTO {
    protected String uid;
    protected String deviceUid;
    protected Integer maxMessagesNumber;
    protected Integer maxMessageSize;
    protected String messagePrototype;
}