// BufferServiceClient.java
package com.service.bufferdevice.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.connection.processing.buffer.model.BufferBLM;

@FeignClient(name = "buffer-service", url = "${buffer.service.url}")
public interface BufferServiceClient {
    
    @GetMapping("/buffers/{bufferUid}")
    BufferBLM getBuffer(@RequestParam String accessToken, @PathVariable UUID bufferUid);

    @GetMapping("/health")
    Map<String, Object> healthCheck();
}