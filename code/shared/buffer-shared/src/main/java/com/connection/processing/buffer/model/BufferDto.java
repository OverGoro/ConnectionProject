
package com.connection.processing.buffer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class BufferDto {
    protected String uid;
    protected String deviceUid;
    protected Integer maxMessagesNumber;
    protected Integer maxMessageSize;
    protected String messagePrototype;
}
