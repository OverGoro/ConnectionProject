// BufferDTO.java
package com.connection.processing.buffer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferDTO {
    @NonNull
    protected String uid;
    @NonNull
    protected String connectionSchemeUid;
    @NonNull
    protected Integer maxMessagesNumber;
    @NonNull
    protected Integer maxMessageSize;
    @NonNull
    protected String messagePrototype;
}