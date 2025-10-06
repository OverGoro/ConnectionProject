// DeviceServiceClient.java
package com.service.bufferdevice.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.connection.device.model.DeviceBLM;

@FeignClient(name = "device-service", url = "${device.service.url}")
public interface DeviceServiceClient {
    
    @GetMapping("/devices/{deviceUid}")
    DeviceBLM getDevice(@RequestParam String accessToken, @PathVariable UUID deviceUid);

    @GetMapping("/health")
    Map<String, Object> healthCheck();
}