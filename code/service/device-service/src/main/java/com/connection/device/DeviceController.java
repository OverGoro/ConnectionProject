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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connection.device.config.SecurityUtils;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;
import com.connection.device.validator.DeviceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device-service")
public class DeviceController {
    
    private final DeviceService deviceService;
    private final DeviceValidator deviceValidator;
    private final DeviceConverter deviceConverter;

    @PostMapping("/devices")
    public ResponseEntity<DeviceBLM> createDevice(@RequestBody DeviceDTO deviceDTO) {
        UUID clientUuid = SecurityUtils.getCurrentClientUid();
        log.info("Creating device for client " + clientUuid);

        deviceValidator.validate(deviceDTO);
        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);

        DeviceBLM device = deviceService.createDevice(deviceBLM);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/devices/{deviceUid}")
    public ResponseEntity<DeviceBLM> getDevice(@PathVariable UUID deviceUid) {
        log.info("Getting device: {}", deviceUid);

        DeviceBLM device = deviceService.getDevice(deviceUid);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceBLM>> getDevicesByClient() {
        log.info("Getting all devices for client");
        
        List<DeviceBLM> devices = deviceService.getDevicesByClient();
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/device")
    public ResponseEntity<DeviceBLM> updateDevice(
            @RequestBody DeviceDTO deviceDTO) {            
        deviceValidator.validate(deviceDTO);
        UUID clientUuid = SecurityUtils.getCurrentClientUid();
        log.info("Updating device: {}, for client: {}", deviceDTO.getUid(), clientUuid);
        

        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);
        DeviceBLM device = deviceService.updateDevice(deviceBLM);
        return ResponseEntity.ok(device);
    }

    @DeleteMapping("/devices/{deviceUid}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID deviceUid) {
        log.info("Deleting device: {}", deviceUid);
        
        deviceService.deleteDevice(deviceUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: device-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(deviceService.getHealthStatus());
    }
}