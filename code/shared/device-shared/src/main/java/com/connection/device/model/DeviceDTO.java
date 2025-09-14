// DeviceDTO.java
package com.connection.device.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeviceDTO {
    @NonNull
    protected String uid;
    @NonNull
    protected String clientUuid;
    @NonNull
    protected String deviceName;
    @NonNull
    protected String deviceDescription;
}