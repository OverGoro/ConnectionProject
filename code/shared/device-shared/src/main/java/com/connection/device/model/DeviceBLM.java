// DeviceBLM.java
package com.connection.device.model;

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
public class DeviceBLM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID clientUuid;
    @NonNull
    protected String deviceName;
    @NonNull
    protected String deviceDescription;
}