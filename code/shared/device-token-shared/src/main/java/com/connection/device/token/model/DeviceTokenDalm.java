
package com.connection.device.token.model;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**  . */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class DeviceTokenDalm {
    protected UUID uid;
    protected UUID deviceUid;
    protected String token;
    protected Date createdAt;
    protected Date expiresAt;
}