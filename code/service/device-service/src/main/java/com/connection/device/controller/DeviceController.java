// DeviceController.java
package com.connection.device.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.device.DeviceService;
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

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 1000;

    @PostMapping("/devices")
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceDTO deviceDTO) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Creating device for client {}", clientUid);

        deviceValidator.validate(deviceDTO);
        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);
        DeviceBLM device = deviceService.createDevice(clientUid, deviceBLM);

        return ResponseEntity.ok(new DeviceResponse(device.getUid()));
    }

    @GetMapping("/devices/{deviceUid}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable UUID deviceUid) {
        log.info("Getting device: {}", deviceUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        DeviceBLM device = deviceService.getDevice(clientUid, deviceUid);

        return ResponseEntity.ok(new DeviceResponse(device.getUid()));
    }

    @GetMapping("/devices")
    public ResponseEntity<DevicesListResponse> getDevicesByClient(
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        
        log.info("Getting all devices for client with offset: {}, limit: {}", offset, limit);
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<DeviceBLM> allDevices = deviceService.getDevicesByClient(clientUid);
        
        // Применяем пагинацию
        List<DeviceBLM> paginatedDevices = applyPagination(allDevices, offset, limit);
        List<DeviceDTO> deviceDTOs = paginatedDevices.stream()
                .map(deviceConverter::toDTO)
                .collect(Collectors.toList());

        // Создаем информацию о пагинации
        DevicesListResponse.PaginationInfo paginationInfo = 
            new DevicesListResponse.PaginationInfo(
                offset, 
                limit, 
                allDevices.size(), 
                (offset + limit) < allDevices.size()
            );

        return ResponseEntity.ok(new DevicesListResponse(deviceDTOs, paginationInfo));
    }

    @PutMapping("/devices/{deviceUid}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable UUID deviceUid,
            @RequestBody DeviceDTO deviceDTO) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Updating device: {} for client: {}", deviceUid, clientUid);

        deviceValidator.validate(deviceDTO);
        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);
        DeviceBLM device = deviceService.updateDevice(clientUid, deviceBLM);

        return ResponseEntity.ok(new DeviceResponse(device.getUid()));
    }

    @DeleteMapping("/devices/{deviceUid}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID deviceUid) {
        log.info("Deleting device: {}", deviceUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        deviceService.deleteDevice(clientUid, deviceUid);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: status: OK, service: device-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(deviceService.getHealthStatus().toString()));
    }

    /**
     * Применяет пагинацию к списку устройств
     * 
     * @param devices полный список устройств
     * @param offset смещение (начальная позиция)
     * @param limit максимальное количество элементов
     * @return пагинированный список устройств
     */
    private List<DeviceBLM> applyPagination(List<DeviceBLM> devices, int offset, int limit) {
        // Валидация параметров пагинации
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }
        
        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        
        // Применяем пагинацию
        return devices.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
}