// BufferJsonDataBLM.java
package com.connection.processing.buffer.objects.json.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferJsonDataBLM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID bufferUid;
    @NonNull
    protected String data;
    @NonNull
    protected Instant createdAt;
}