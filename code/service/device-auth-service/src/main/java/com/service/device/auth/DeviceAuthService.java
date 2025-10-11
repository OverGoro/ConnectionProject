// DeviceAuthService.java
package com.service.device.auth;

import java.util.UUID;

import org.springframework.data.util.Pair;

import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenBLM;

public interface DeviceAuthService {
    // Device Token operations
    DeviceTokenBLM createDeviceToken(UUID deviceUid);
    DeviceTokenBLM getDeviceToken(UUID deviceUid);
    void revokeDeviceToken(UUID deviceUid);
    void validateDeviceToken(DeviceTokenBLM deviceToken);
    
    // Device Access Token operations
    Pair<DeviceAccessTokenBLM, DeviceTokenBLM> createDeviceAccessToken(DeviceTokenBLM deviceToken);
    DeviceAccessTokenBLM refreshDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken);
    void validateDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken);
    
    // Utility methods
    UUID extractDeviceUidFromToken(DeviceTokenBLM deviceToken);
    UUID extractDeviceUidFromAccessToken(DeviceAccessTokenBLM deviceAccessToken);
}