// BufferJsonDataBLM.java
package com.connection.processing.buffer.objects.json.model;

import java.time.Instant;
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
public class BufferJsonDataBLM {
    
    protected UUID uid;
    
    protected UUID bufferUid;
    
    protected String data;
    
    protected Instant createdAt;
}