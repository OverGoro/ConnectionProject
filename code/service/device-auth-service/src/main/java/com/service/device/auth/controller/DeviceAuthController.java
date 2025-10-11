// DeviceAuthController.java
package com.service.device.auth.controller;

import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDTO;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDTO;
import com.service.device.auth.DeviceAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device-auth")
public class DeviceAuthController {
    
    private final DeviceAuthService deviceAuthService;
    private final DeviceTokenConverter deviceTokenConverter;
    private final DeviceAccessTokenConverter deviceAccessTokenConverter;

    @PostMapping("/device-token")
    public ResponseEntity<DeviceTokenResponse> createDeviceToken(@RequestBody CreateDeviceTokenRequest request) {
        log.info("Creating device token for device: {}", request.deviceUid());
        
        DeviceTokenBLM deviceToken = deviceAuthService.createDeviceToken(request.deviceUid());
        
        return ResponseEntity.ok(new DeviceTokenResponse(
            deviceToken.getToken(),
            deviceToken.getExpiresAt(),
            deviceToken.getDeviceUid()
        ));
    }

    @GetMapping("/device-token")
    public ResponseEntity<DeviceTokenResponse> getDeviceToken(@RequestParam UUID deviceUid) {
        log.info("Getting device token for device: {}", deviceUid);
        
        DeviceTokenBLM deviceToken = deviceAuthService.getDeviceToken(deviceUid);
        
        return ResponseEntity.ok(new DeviceTokenResponse(
            deviceToken.getToken(),
            deviceToken.getExpiresAt(),
            deviceToken.getDeviceUid()
        ));
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<Void> revokeDeviceToken(@RequestParam UUID deviceUid) {
        log.info("Revoking device token for device: {}", deviceUid);
        
        deviceAuthService.revokeDeviceToken(deviceUid);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/access-token")
    public ResponseEntity<DeviceAccessTokenResponse> createDeviceAccessToken(@RequestBody DeviceTokenDTO deviceTokenDTO) {
        log.info("Creating device access token");
        
        DeviceTokenBLM deviceToken = deviceTokenConverter.toBLM(deviceTokenDTO);
        Pair<DeviceAccessTokenBLM, DeviceTokenBLM> result = deviceAuthService.createDeviceAccessToken(deviceToken);
        
        return ResponseEntity.ok(new DeviceAccessTokenResponse(
            result.getFirst().getToken(),
            result.getFirst().getExpiresAt(),
            result.getSecond().getDeviceUid()
        ));
    }

    @PostMapping("/access-token/refresh")
    public ResponseEntity<DeviceAccessTokenResponse> refreshDeviceAccessToken(@RequestBody DeviceAccessTokenDTO deviceAccessTokenDTO) {
        log.info("Refreshing device access token");
        
        DeviceAccessTokenBLM deviceAccessToken = deviceAccessTokenConverter.toBLM(deviceAccessTokenDTO);
        DeviceAccessTokenBLM newAccessToken = deviceAuthService.refreshDeviceAccessToken(deviceAccessToken);
        
        return ResponseEntity.ok(new DeviceAccessTokenResponse(
            newAccessToken.getToken(),
            newAccessToken.getExpiresAt(),
            deviceAuthService.extractDeviceUidFromAccessToken(newAccessToken)
        ));
    }

    @GetMapping("/validate/device-token")
    public ResponseEntity<ValidationResponse> validateDeviceToken(@RequestParam String deviceToken) {
        log.info("Validating device token");
        
        DeviceTokenDTO deviceTokenDTO = new DeviceTokenDTO(deviceToken);
        DeviceTokenBLM deviceTokenBLM = deviceTokenConverter.toBLM(deviceTokenDTO);
        deviceAuthService.validateDeviceToken(deviceTokenBLM);
        
        return ResponseEntity.ok(new ValidationResponse("VALID"));
    }

    @GetMapping("/validate/access-token")
    public ResponseEntity<ValidationResponse> validateAccessToken(@RequestParam String accessToken) {
        log.info("Validating device access token");
        
        DeviceAccessTokenDTO accessTokenDTO = new DeviceAccessTokenDTO(accessToken);
        DeviceAccessTokenBLM accessTokenBLM = deviceAccessTokenConverter.toBLM(accessTokenDTO);
        deviceAuthService.validateDeviceAccessToken(accessTokenBLM);
        
        return ResponseEntity.ok(new ValidationResponse("VALID"));
    }

    @GetMapping("/extract/device-uid/device-token")
    public ResponseEntity<UUID> extractDeviceUidFromDeviceToken(@RequestParam String deviceToken) {
        log.info("Extracting device UID from device token");
        
        DeviceTokenDTO deviceTokenDTO = new DeviceTokenDTO(deviceToken);
        DeviceTokenBLM deviceTokenBLM = deviceTokenConverter.toBLM(deviceTokenDTO);
        UUID deviceUid = deviceAuthService.extractDeviceUidFromToken(deviceTokenBLM);
        
        return ResponseEntity.ok(deviceUid);
    }

    @GetMapping("/extract/device-uid/access-token")
    public ResponseEntity<UUID> extractDeviceUidFromAccessToken(@RequestParam String accessToken) {
        log.info("Extracting device UID from access token");
        
        DeviceAccessTokenDTO accessTokenDTO = new DeviceAccessTokenDTO(accessToken);
        DeviceAccessTokenBLM accessTokenBLM = deviceAccessTokenConverter.toBLM(accessTokenDTO);
        UUID deviceUid = deviceAuthService.extractDeviceUidFromAccessToken(accessTokenBLM);
        
        return ResponseEntity.ok(deviceUid);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check");
        
        return ResponseEntity.ok(new HealthResponse(
            "OK",
            "device-auth-service",
            System.currentTimeMillis()
        ));
    }

    // DTO классы
    public record CreateDeviceTokenRequest(UUID deviceUid) {}
    
    public record DeviceTokenResponse(String token, Object expiresAt, UUID deviceUid) {}
    
    public record DeviceAccessTokenResponse(String token, Object expiresAt, UUID deviceUid) {}
    
    public record ValidationResponse(String status) {}
    
    public record HealthResponse(String status, String service, long timestamp) {}
}