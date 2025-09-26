package com.service.device.auth;

import java.util.UUID;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;



public interface DeviceAuthService {
    public DeviceAccessTokenBLM authorizeByToken(DeviceTokenBLM deviceToken);
    public void validateDeviceAccessToken(DeviceAccessTokenBLM deviceAccessTokenBLM);
    public void validateDeviceToken(DeviceTokenBLM deviceAccessTokenBLM);

    public UUID getDeviceUid(DeviceAccessTokenBLM accessTokenBLM);
    public UUID getDeviceUid(DeviceTokenBLM accessTokenBLM);
}
