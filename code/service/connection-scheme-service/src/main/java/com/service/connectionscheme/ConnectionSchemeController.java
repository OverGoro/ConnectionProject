// ConnectionSchemeController.java
package com.service.connectionscheme;

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

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;
import com.service.connectionscheme.config.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/connection-scheme-service")
public class ConnectionSchemeController {
    
    private final ConnectionSchemeService connectionSchemeService;

    @PostMapping("/schemes")
    public ResponseEntity<ConnectionSchemeBLM> createScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ConnectionSchemeDTO schemeDTO) {
        
        log.info("Creating connection scheme for client");

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        ConnectionSchemeBLM scheme = connectionSchemeService.createScheme(clientUid, schemeDTO);
        return ResponseEntity.ok(scheme);
    }

    @GetMapping("/schemes/{schemeUid}")
    public ResponseEntity<ConnectionSchemeBLM> getScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID schemeUid) {
        
        
        log.info("Getting connection scheme: {}", schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        ConnectionSchemeBLM scheme = connectionSchemeService.getSchemeByUid(clientUid, schemeUid);
        return ResponseEntity.ok(scheme);
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<ConnectionSchemeBLM>> getSchemesByClient(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        
        log.info("Getting all connection schemes for client");
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        List<ConnectionSchemeBLM> schemes = connectionSchemeService.getSchemesByClient(clientUid);
        return ResponseEntity.ok(schemes);
    }

    @PutMapping("/schemes/{schemeUid}")
    public ResponseEntity<ConnectionSchemeBLM> updateScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID schemeUid,
            @RequestBody ConnectionSchemeDTO schemeDTO) {
        
        
        log.info("Updating connection scheme: {}", schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        ConnectionSchemeBLM scheme = connectionSchemeService.updateScheme(clientUid, schemeUid, schemeDTO);
        return ResponseEntity.ok(scheme);
    }

    @DeleteMapping("/schemes/{schemeUid}")
    public ResponseEntity<Void> deleteScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID schemeUid) {
        
        
        log.info("Deleting connection scheme: {}", schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        connectionSchemeService.deleteScheme(clientUid, schemeUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: connection-scheme-service, timestamp: {}", 
                System.currentTimeMillis());
                
        return ResponseEntity.ok().body(connectionSchemeService.getHealthStatus());
    }

}