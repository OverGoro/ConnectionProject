// DeviceAuthService.java
package com.service.device.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.util.Pair;

import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenBLM;

public interface DeviceAuthService {
    // Device Token operations
    DeviceTokenBLM createDeviceToken(UUID deviceUid);
    DeviceTokenBLM getDeviceToken(UUID deviceUid);
    void revokeDeviceToken(UUID deviceUid);
    DeviceTokenBLM validateDeviceToken(DeviceTokenBLM deviceToken);
    DeviceTokenBLM validateDeviceToken(String deviceTokenString);
    
    // Device Access Token operations
    Pair<DeviceAccessTokenBLM, DeviceTokenBLM> createDeviceAccessToken(DeviceTokenBLM deviceToken);
    DeviceAccessTokenBLM refreshDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken);
    DeviceAccessTokenBLM validateDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken);
    DeviceAccessTokenBLM validateDeviceAccessToken(String deviceAccessTokenString);
        
    
    Map<String, Object> getHealthStatus();


}