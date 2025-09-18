// BufferDeviceDALM.java
package com.connection.processing.buffer.bufferdevice.model;

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
public class BufferDeviceDALM {
    @NonNull
    protected UUID bufferUid;
    @NonNull
    protected UUID deviceUid;
}