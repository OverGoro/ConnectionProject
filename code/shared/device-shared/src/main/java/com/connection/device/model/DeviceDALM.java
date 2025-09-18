// DeviceDALM.java
package com.connection.device.model;

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
public class DeviceDALM {
    
    protected UUID uid;
    
    protected UUID clientUuid;
    
    protected String deviceName;
    
    protected String deviceDescription;
}