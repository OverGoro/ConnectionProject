
package com.connection.device.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class DeviceDto {

    protected String uid;

    protected String clientUuid;

    protected String deviceName;

    protected String deviceDescription;
}
