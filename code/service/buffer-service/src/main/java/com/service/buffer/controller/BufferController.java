// BufferController.java
package com.service.buffer.controller;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;
import com.service.buffer.BufferService;
import com.service.buffer.config.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buffer-service")
public class BufferController {
    
    private final BufferService bufferService;
    private final BufferConverter bufferConverter;

    @PostMapping("/buffers")
    public ResponseEntity<?> createBuffer(
            @RequestBody CreateBufferRequest createRequest) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Creating buffer for client: {}, connection scheme: {}", 
                clientUid, createRequest.getConnectionSchemeUid());
        
        // Конвертируем CreateBufferRequest в BufferDTO
        BufferDTO bufferDTO = createRequest.getBufferDTO();
        BufferBLM buffer = bufferService.createBuffer(clientUid, bufferDTO);
        
        log.info("Created buffer with uid {} for client {}", buffer.getUid(), clientUid);
        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @GetMapping("/buffers/{bufferUid}")
    public ResponseEntity<?> getBuffer(
            @PathVariable UUID bufferUid) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Getting buffer: {} for client: {}", bufferUid, clientUid);
        
        BufferBLM buffer = bufferService.getBuffer(authorizationHeader, bufferUid);

        log.info("Got buffer: {} for client: {}", buffer.getUid(), clientUid);
        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @GetMapping("/buffers")
    public ResponseEntity<?> getBuffersByClient() {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Getting all buffers for client: {}", clientUid);
        
        List<BufferBLM> buffers = bufferService.getBuffersByClient(clientUid);
        List<BufferDTO> bufferDTOs = buffers.stream()
            .map(buffer -> bufferConverter.toDTO(buffer))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }

    @GetMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<?> getBuffersByConnectionScheme(
            @PathVariable UUID connectionSchemeUid) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Getting buffers for connection scheme: {} for client: {}", connectionSchemeUid, clientUid);
        
        List<BufferBLM> buffers = bufferService.getBuffersByConnectionScheme(clientUid, connectionSchemeUid);
        List<BufferDTO> bufferDTOs = buffers.stream()
            .map(buffer -> bufferConverter.toDTO(buffer))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }

    @PutMapping("/buffers/{bufferUid}")
    public ResponseEntity<?> updateBuffer(
            @PathVariable UUID bufferUid,
            @RequestBody UpdateBufferRequest updateRequest) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Updating buffer: {} for client: {}", bufferUid, clientUid);
        
        // Конвертируем UpdateBufferRequest в BufferDTO
        BufferDTO bufferDTO = updateRequest.getBufferDTO();
        BufferBLM buffer = bufferService.updateBuffer(clientUid, bufferUid, bufferDTO);
        
        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @DeleteMapping("/buffers/{bufferUid}")
    public ResponseEntity<?> deleteBuffer(
            @PathVariable UUID bufferUid) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Deleting buffer: {} for client: {}", bufferUid, clientUid);
        
        bufferService.deleteBuffer(authorizationHeader, bufferUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<?> deleteBuffersByConnectionScheme(
            @PathVariable UUID connectionSchemeUid) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Deleting buffers for connection scheme: {} for client: {}", connectionSchemeUid, clientUid);
        
        bufferService.deleteBuffersByConnectionScheme(authorizationHeader, connectionSchemeUid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check - status: OK, service: buffer-service, timestamp: {}", 
                System.currentTimeMillis());

        // Предполагая, что bufferService.getHealthStatus() возвращает HealthResponse
        return ResponseEntity.ok().body(bufferService.getHealthStatus());
    }
}
