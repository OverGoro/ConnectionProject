// BufferDeviceDTO.java
package com.connection.processing.buffer.bufferdevice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BufferDeviceDTO {
    @NonNull
    protected String bufferUid;
    @NonNull
    protected String deviceUid;
}