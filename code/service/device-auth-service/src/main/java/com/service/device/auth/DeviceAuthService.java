
package com.service.device.auth;

import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceTokenBlm;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.util.Pair;

/** . */
public interface DeviceAuthService {
    /** . */
    DeviceTokenBlm createDeviceToken(UUID deviceUid);

    /** . */
    DeviceTokenBlm getDeviceToken(UUID deviceUid);

    /** . */
    void revokeDeviceToken(UUID deviceUid);

    /** . */
    DeviceTokenBlm validateDeviceToken(DeviceTokenBlm deviceToken);

    /** . */
    DeviceTokenBlm validateDeviceToken(String deviceTokenString);

    /** . */
    Pair<DeviceAccessTokenBlm, DeviceTokenBlm> createDeviceAccessToken(
            DeviceTokenBlm deviceToken);

    /** . */
    DeviceAccessTokenBlm refreshDeviceAccessToken(
            DeviceAccessTokenBlm deviceAccessToken);

    /** . */
    DeviceAccessTokenBlm validateDeviceAccessToken(
            DeviceAccessTokenBlm deviceAccessToken);

    /** . */
    DeviceAccessTokenBlm validateDeviceAccessToken(
            String deviceAccessTokenString);

    /** . */
    Map<String, Object> getHealthStatus();
}
