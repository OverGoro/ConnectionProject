// BufferJsonDataDTO.java
package com.connection.processing.buffer.objects.json.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferJsonDataDTO {
    @NonNull
    protected String uid;
    @NonNull
    protected String bufferUid;
    @NonNull
    protected String data;
    @NonNull
    protected Instant createdAt;
}