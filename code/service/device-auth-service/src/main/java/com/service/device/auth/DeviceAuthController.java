// DeviceAuthController.java
package com.service.device.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device-auth-service")
public class DeviceAuthController {
    
    private final DeviceAuthService deviceAuthService;

    @PostMapping("/authorize")
    public ResponseEntity<DeviceAccessTokenBLM> authorizeByToken(
            @RequestBody DeviceTokenDTO deviceTokenDTO) {
        
        log.info("Device authorization request");
        
        DeviceTokenBLM deviceTokenBLM = DeviceTokenBLM.builder()
                .token(deviceTokenDTO.getToken())
                .build();
        
        DeviceAccessTokenBLM deviceAccessToken = deviceAuthService.authorizeByToken(deviceTokenBLM);
        
        log.info("Device authorized successfully");
        return ResponseEntity.ok(deviceAccessToken);
    }


    @PostMapping("/validate/access-token")
    public ResponseEntity<Void> validateDeviceAccessToken(
            @RequestBody DeviceTokenDTO deviceTokenDTO) {
        
        log.debug("Device access token validation request");
        
        DeviceAccessTokenBLM deviceAccessTokenBLM = DeviceAccessTokenBLM.builder()
                .token(deviceTokenDTO.getToken())
                .build();
        
        deviceAuthService.validateDeviceAccessToken(deviceAccessTokenBLM);
        
        log.debug("Device access token validated successfully");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate/device-token")
    public ResponseEntity<Void> validateDeviceToken(
            @RequestBody DeviceTokenDTO deviceTokenDTO) {
        
        log.debug("Device token validation request");
        
        DeviceTokenBLM deviceTokenBLM = DeviceTokenBLM.builder()
                .token(deviceTokenDTO.getToken())
                .build();
        
        deviceAuthService.validateDeviceToken(deviceTokenBLM);
        
        log.debug("Device token validated successfully");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/extract/device-uid")
    public ResponseEntity<UUID> getDeviceUidFromAccessToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.debug("Extracting device UID from access token");
        
        String token = extractToken(authorizationHeader);
        
        DeviceAccessTokenBLM deviceAccessTokenBLM = DeviceAccessTokenBLM.builder()
                .token(token)
                .build();
        
        UUID deviceUid = deviceAuthService.getDeviceUid(deviceAccessTokenBLM);
        
        log.debug("Device UID extracted successfully: {}", deviceUid);
        return ResponseEntity.ok(deviceUid);
    }

    @PostMapping("/extract/device-uid/device-token")
    public ResponseEntity<UUID> getDeviceUidFromDeviceToken(
            @RequestBody DeviceTokenDTO deviceTokenDTO) {
        
        log.debug("Extracting device UID from device token");
        
        DeviceTokenBLM deviceTokenBLM = DeviceTokenBLM.builder()
                .token(deviceTokenDTO.getToken())
                .build();
        
        UUID deviceUid = deviceAuthService.getDeviceUid(deviceTokenBLM);
        
        log.debug("Device UID extracted successfully: {}", deviceUid);
        return ResponseEntity.ok(deviceUid);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: device-auth-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(Map.of(
            "status", "OK",
            "service", "device-auth-service",
            "timestamp", System.currentTimeMillis()
        ));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}