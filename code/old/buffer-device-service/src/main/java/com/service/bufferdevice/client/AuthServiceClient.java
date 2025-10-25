// AuthServiceClient.java
package com.service.bufferdevice.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {
    
    @GetMapping("/extract/accessTokenClientUID")
    UUID getAccessTokenClientUID(@RequestParam String accessToken);
    
    @GetMapping("/validate/token/access")
    void validateAccessToken(@RequestParam String accessToken);

    @GetMapping("/health")
    Map<String, Object> healthCheck();
}