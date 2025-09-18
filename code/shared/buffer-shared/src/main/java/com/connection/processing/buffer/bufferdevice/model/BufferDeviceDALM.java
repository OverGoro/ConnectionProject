// BufferDeviceDALM.java
package com.connection.processing.buffer.bufferdevice.model;

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
public class BufferDeviceDALM {
    
    protected UUID bufferUid;
    
    protected UUID deviceUid;
}