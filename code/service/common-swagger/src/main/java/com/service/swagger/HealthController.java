package com.service.swagger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("API Gateway health check");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "api-gateway");
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(healthStatus);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "API Gateway");
        info.put("description", "Aggregates all microservice APIs");
        info.put("version", "1.0.0");
        
        return ResponseEntity.ok(info);
    }
}