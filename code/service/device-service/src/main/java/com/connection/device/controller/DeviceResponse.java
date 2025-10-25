// DeviceResponse.java
package com.connection.device.controller;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeviceResponse {
    private final UUID deviceUid;
}