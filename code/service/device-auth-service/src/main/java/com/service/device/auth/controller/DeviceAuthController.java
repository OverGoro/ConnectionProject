package com.service.device.auth.controller;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDto;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDto;
import com.connection.device.token.util.TokenUtils;
import com.service.device.auth.DeviceAuthService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** . */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/device/auth")
public class DeviceAuthController {

    private final DeviceAuthService deviceAuthService;
    private final DeviceTokenConverter deviceTokenConverter;
    private final DeviceAccessTokenConverter deviceAccessTokenConverter;

    /** . */
    @PostMapping("/device-token")
    public ResponseEntity<DeviceTokenResponse> createDeviceToken(
            @RequestBody CreateDeviceTokenRequest request) {
        log.info("Creating device token for device: {}", request.deviceUid());

        DeviceTokenBlm deviceToken =
                deviceAuthService.createDeviceToken(request.deviceUid());

        return ResponseEntity.ok(new DeviceTokenResponse(deviceToken.getToken(),
                deviceToken.getExpiresAt(), deviceToken.getDeviceUid()));
    }

    /** . */
    @GetMapping("/device-token")
    public ResponseEntity<DeviceTokenResponse> getDeviceToken(
            @RequestParam(required = false) UUID deviceUid) {

        DeviceTokenBlm deviceToken;

        if (deviceUid != null) {
            log.info("Getting device token for device: {}", deviceUid);
            deviceToken = deviceAuthService.getDeviceToken(deviceUid);
        } else {
            log.warn("No valid parameters provided for getting device token");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(new DeviceTokenResponse(deviceToken.getToken(),
                deviceToken.getExpiresAt(), deviceToken.getDeviceUid()));
    }

    /** . */
    @DeleteMapping("/device-token")
    public ResponseEntity<Void> revokeDeviceToken(
            @RequestParam(required = false) List<UUID> deviceUids) {

        if (deviceUids != null && !deviceUids.isEmpty()) {
            // Массовое удаление токенов по deviceUids
            log.info("Revoking device tokens for devices: {}", deviceUids);
            for (UUID uid : deviceUids) {
                deviceAuthService.revokeDeviceToken(uid);
            }
        } else {
            log.warn("No valid parameters provided for revoking device token");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    /** . */
    @PostMapping("/access-token")
    public ResponseEntity<DeviceAccessTokenResponse> createDeviceAccessToken(
            @RequestBody DeviceTokenDto deviceTokenDto) {
        log.info("Creating device access token");

        DeviceTokenBlm deviceToken = deviceTokenConverter.toBlm(deviceTokenDto);

        log.info(deviceToken.getToken() + " \n" + deviceToken.getCreatedAt()
                + " \n" + deviceToken.getDeviceUid() + " \n"
                + deviceToken.getExpiresAt() + " \n" + deviceToken.getUid());

        Pair<DeviceAccessTokenBlm, DeviceTokenBlm> result =
                deviceAuthService.createDeviceAccessToken(deviceToken);

        return ResponseEntity.ok(new DeviceAccessTokenResponse(
                result.getFirst().getToken(), result.getFirst().getExpiresAt(),
                result.getSecond().getDeviceUid()));
    }

    /** . */
    @PostMapping("/access-token/refresh")
    public ResponseEntity<DeviceAccessTokenResponse> refreshDeviceAccessToken(
            @RequestBody DeviceAccessTokenDto deviceAccessTokenDto) {
        log.info("Refreshing device access token");

        DeviceAccessTokenBlm deviceAccessToken =
                deviceAccessTokenConverter.toBlm(deviceAccessTokenDto);
        DeviceAccessTokenBlm newAccessToken =
                deviceAuthService.refreshDeviceAccessToken(deviceAccessToken);

        return ResponseEntity.ok(new DeviceAccessTokenResponse(
                newAccessToken.getToken(), newAccessToken.getExpiresAt(),
                TokenUtils.extractDeviceUidFromDeviceToken(
                        newAccessToken.getToken())));
    }

    /** . */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(
            @RequestParam(required = false) String deviceToken,
            @RequestParam(required = false) String accessToken) {

        if (deviceToken != null) {
            log.info("Validating device token");
            DeviceTokenDto deviceTokenDto = new DeviceTokenDto(deviceToken);
            DeviceTokenBlm deviceTokenBlm =
                    deviceTokenConverter.toBlm(deviceTokenDto);
            deviceAuthService.validateDeviceToken(deviceTokenBlm);
            return ResponseEntity
                    .ok(new ValidationResponse("DEVICE_TOKEN_VALID"));
        } else if (accessToken != null) {
            log.info("Validating device access token");
            log.info(accessToken);
            DeviceAccessTokenDto accessTokenDto =
                    new DeviceAccessTokenDto(accessToken);
            log.info(accessTokenDto.getToken());

            DeviceAccessTokenBlm accessTokenBlm =
                    deviceAccessTokenConverter.toBlm(accessTokenDto);
            log.info(accessTokenBlm.getToken().toString() + " "
                    + accessTokenBlm.getCreatedAt().toString() + " "
                    + accessTokenBlm.getExpiresAt().toString() + " "
                    + accessTokenBlm.getDeviceTokenUid().toString() + " "
                    + accessTokenBlm.getUid());

            deviceAuthService.validateDeviceAccessToken(accessTokenBlm);

            return ResponseEntity
                    .ok(new ValidationResponse("ACCESS_TOKEN_VALID"));
        } else {
            log.warn("No valid parameters provided for token validation");
            return ResponseEntity.badRequest().build();
        }
    }

    /** . */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info(
                "Health check: status: OK, service: device-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(
                deviceAuthService.getHealthStatus().toString()));
    }

    /** . */
    public record CreateDeviceTokenRequest(UUID deviceUid) {
    }

    /** . */
    public record DeviceTokenResponse(String token, Object expiresAt,
            UUID deviceUid) {
    }

    /** . */
    public record DeviceAccessTokenResponse(String token, Object expiresAt,
            UUID deviceUid) {
    }

    /** . */
    public record ValidationResponse(String status) {
    }

    /** . */
    public record HealthResponse(String message) {
    }
}
