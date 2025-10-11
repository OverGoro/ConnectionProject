// DeviceBLM.java
package com.connection.device.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class DeviceBLM {

    protected UUID uid;

    protected UUID clientUuid;

    protected String deviceName;

    protected String deviceDescription;
}