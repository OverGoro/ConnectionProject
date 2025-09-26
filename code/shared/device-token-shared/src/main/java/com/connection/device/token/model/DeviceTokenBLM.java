// DeviceTokenBLM.java
package com.connection.device.token.model;

import java.util.Date;
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
public class DeviceTokenBLM {
    protected String token;
    protected UUID uid;
    protected UUID deviceUid;
    protected Date createdAt;
    protected Date expiresAt;
}