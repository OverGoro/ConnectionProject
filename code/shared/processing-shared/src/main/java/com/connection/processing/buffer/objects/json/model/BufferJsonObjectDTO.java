// BufferJsonObjectDTO.java
package com.connection.processing.buffer.objects.json.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * DTO для JSON объекта буфера
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferJsonObjectDTO {
    @NonNull
    protected String uid;
    @NonNull
    protected String bufferUid;
    @NonNull
    protected String data;
}