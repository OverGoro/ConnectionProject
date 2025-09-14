// BufferJsonObjectDALM.java
package com.connection.processing.buffer.objects.json.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * DALM для JSON объекта буфера
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferJsonObjectDALM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID bufferUid;
    @NonNull
    protected String data;
}