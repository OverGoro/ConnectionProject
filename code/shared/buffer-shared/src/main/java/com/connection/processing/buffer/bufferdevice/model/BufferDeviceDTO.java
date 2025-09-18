// BufferDeviceDTO.java
package com.connection.processing.buffer.bufferdevice.model;

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
public class BufferDeviceDTO {
    
    protected String bufferUid;
    
    protected String deviceUid;
}