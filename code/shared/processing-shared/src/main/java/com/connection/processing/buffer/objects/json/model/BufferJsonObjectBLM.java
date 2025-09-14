// BufferJsonObjectBLM.java
package com.connection.processing.buffer.objects.json.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * BLM для JSON объекта буфера
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferJsonObjectBLM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID bufferUid;
    @NonNull
    protected String data;
}