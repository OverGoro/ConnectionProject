// BufferJsonDataDTO.java
package com.connection.processing.buffer.objects.json.model;

import java.time.Instant;

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
public class BufferJsonDataDTO {
    
    protected String uid;
    
    protected String bufferUid;
    
    protected String data;
    
    protected Instant createdAt;
}