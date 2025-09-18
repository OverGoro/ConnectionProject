package com.connection.device;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device-service")
public class DeviceController {
    
    private final DeviceService deviceService;

    @PostMapping("/devices")
    public ResponseEntity<DeviceBLM> createDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody DeviceDTO deviceDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Creating device for client");
        
        DeviceBLM device = deviceService.createDevice(accessToken, deviceDTO);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/devices/{deviceUid}")
    public ResponseEntity<DeviceBLM> getDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting device: {}", deviceUid);
        
        DeviceBLM device = deviceService.getDevice(accessToken, deviceUid);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceBLM>> getDevicesByClient(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting all devices for client");
        
        List<DeviceBLM> devices = deviceService.getDevicesByClient(accessToken);
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/devices/{deviceUid}")
    public ResponseEntity<DeviceBLM> updateDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid,
            @RequestBody DeviceDTO deviceDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Updating device: {}", deviceUid);
        
        DeviceBLM device = deviceService.updateDevice(accessToken, deviceUid, deviceDTO);
        return ResponseEntity.ok(device);
    }

    @DeleteMapping("/devices/{deviceUid}")
    public ResponseEntity<Void> deleteDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting device: {}", deviceUid);
        
        deviceService.deleteDevice(accessToken, deviceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: device-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(deviceService.getHealthStatus());
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}