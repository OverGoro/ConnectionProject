// DeviceDTO.java
package com.connection.device.model;

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
public class DeviceDTO {
    
    protected String uid;
    
    protected String clientUuid;
    
    protected String deviceName;
    
    protected String deviceDescription;
}