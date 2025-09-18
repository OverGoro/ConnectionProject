// ConnectionSchemeServiceClient.java
package com.service.buffer.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.connection.scheme.model.ConnectionSchemeBLM;

@FeignClient(name = "connection-scheme-service", url = "${connection.scheme.service.url}")
public interface ConnectionSchemeServiceClient {
    
    @GetMapping("/schemes/{schemeUid}")
    ConnectionSchemeBLM getScheme(@RequestParam String accessToken, @PathVariable UUID schemeUid);
    
    @GetMapping("/schemes")
    List<ConnectionSchemeBLM> getSchemesByClient(@RequestParam String accessToken);

    @GetMapping("/health")
    Map<String, Object> healthCheck();
}