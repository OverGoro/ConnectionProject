// BufferDeviceController.java
package com.service.bufferdevice;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buffer-device-service")
public class BufferDeviceController {
    
    private final BufferDeviceService bufferDeviceService;

    @PostMapping("/bindings")
    public ResponseEntity<BufferDeviceBLM> createBufferDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody BufferDeviceDTO bufferDeviceDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Creating buffer-device binding: buffer={}, device={}", 
                bufferDeviceDTO.getBufferUid(), bufferDeviceDTO.getDeviceUid());
        
        BufferDeviceBLM binding = bufferDeviceService.createBufferDevice(accessToken, bufferDeviceDTO);
        return ResponseEntity.ok(binding);
    }

    @PostMapping("/bindings/buffer/{bufferUid}/devices")
    public ResponseEntity<Void> addDevicesToBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @RequestBody List<UUID> deviceUids) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Adding {} devices to buffer: {}", deviceUids.size(), bufferUid);
        
        bufferDeviceService.addDevicesToBuffer(accessToken, bufferUid, deviceUids);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bindings/device/{deviceUid}/buffers")
    public ResponseEntity<Void> addBuffersToDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid,
            @RequestBody List<UUID> bufferUids) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Adding {} buffers to device: {}", bufferUids.size(), deviceUid);
        
        bufferDeviceService.addBuffersToDevice(accessToken, deviceUid, bufferUids);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bindings/buffer/{bufferUid}/device/{deviceUid}")
    public ResponseEntity<Void> deleteBufferDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @PathVariable UUID deviceUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting buffer-device binding: buffer={}, device={}", bufferUid, deviceUid);
        
        bufferDeviceService.deleteBufferDevice(accessToken, bufferUid, deviceUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bindings/buffer/{bufferUid}")
    public ResponseEntity<Void> deleteAllBufferDevicesForBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting all buffer-device bindings for buffer: {}", bufferUid);
        
        bufferDeviceService.deleteAllBufferDevicesForBuffer(accessToken, bufferUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bindings/device/{deviceUid}")
    public ResponseEntity<Void> deleteAllBufferDevicesForDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting all buffer-device bindings for device: {}", deviceUid);
        
        bufferDeviceService.deleteAllBufferDevicesForDevice(accessToken, deviceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bindings/buffer/{bufferUid}")
    public ResponseEntity<List<BufferDeviceBLM>> getBufferDevicesByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting buffer-device bindings for buffer: {}", bufferUid);
        
        List<BufferDeviceBLM> bindings = bufferDeviceService.getBufferDevicesByBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(bindings);
    }

    @GetMapping("/bindings/device/{deviceUid}")
    public ResponseEntity<List<BufferDeviceBLM>> getBufferDevicesByDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID deviceUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting buffer-device bindings for device: {}", deviceUid);
        
        List<BufferDeviceBLM> bindings = bufferDeviceService.getBufferDevicesByDevice(accessToken, deviceUid);
        return ResponseEntity.ok(bindings);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: buffer-device-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(bufferDeviceService.getHealthStatus());
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}