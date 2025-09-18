// BufferController.java
package com.service.buffer;

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

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buffer-service")
public class BufferController {
    
    private final BufferService bufferService;

    @PostMapping("/buffers")
    public ResponseEntity<BufferBLM> createBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody BufferDTO bufferDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Creating buffer for connection scheme: {}", bufferDTO.getConnectionSchemeUid());
        
        BufferBLM buffer = bufferService.createBuffer(accessToken, bufferDTO);
        return ResponseEntity.ok(buffer);
    }

    @GetMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferBLM> getBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting buffer: {}", bufferUid);
        
        BufferBLM buffer = bufferService.getBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(buffer);
    }

    @GetMapping("/buffers")
    public ResponseEntity<List<BufferBLM>> getBuffersByClient(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting all buffers for client");
        
        List<BufferBLM> buffers = bufferService.getBuffersByClient(accessToken);
        return ResponseEntity.ok(buffers);
    }

    @GetMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<List<BufferBLM>> getBuffersByConnectionScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID connectionSchemeUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting buffers for connection scheme: {}", connectionSchemeUid);
        
        List<BufferBLM> buffers = bufferService.getBuffersByConnectionScheme(accessToken, connectionSchemeUid);
        return ResponseEntity.ok(buffers);
    }

    @PutMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferBLM> updateBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @RequestBody BufferDTO bufferDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Updating buffer: {}", bufferUid);
        
        BufferBLM buffer = bufferService.updateBuffer(accessToken, bufferUid, bufferDTO);
        return ResponseEntity.ok(buffer);
    }

    @DeleteMapping("/buffers/{bufferUid}")
    public ResponseEntity<Void> deleteBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting buffer: {}", bufferUid);
        
        bufferService.deleteBuffer(accessToken, bufferUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<Void> deleteBuffersByConnectionScheme(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID connectionSchemeUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting buffers for connection scheme: {}", connectionSchemeUid);
        
        bufferService.deleteBuffersByConnectionScheme(accessToken, connectionSchemeUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: buffer-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(bufferService.getHealthStatus());
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}