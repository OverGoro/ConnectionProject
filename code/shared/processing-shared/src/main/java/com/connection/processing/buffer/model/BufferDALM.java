// BufferDALM.java
package com.connection.processing.buffer.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * DALM для буфера
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferDALM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID connectionSchemeUid;
}