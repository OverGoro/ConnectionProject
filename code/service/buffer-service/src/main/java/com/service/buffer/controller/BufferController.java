// BufferController.java
package com.service.buffer.controller;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RestController;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.validator.BufferValidator;
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
    private final BufferValidator bufferValidator;
    private final BufferConverter bufferConverter;

    @PostMapping("/buffers")
    public ResponseEntity<BufferResponse> createBuffer(@RequestBody BufferDTO bufferDTO) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Creating buffer for client {}", clientUid);

        bufferValidator.validate(bufferDTO);
        BufferBLM buffer = bufferService.createBuffer(clientUid, bufferDTO);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @GetMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferResponse> getBuffer(@PathVariable UUID bufferUid) {
        log.info("Getting buffer: {}", bufferUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        BufferBLM buffer = bufferService.getBufferByUid(clientUid, bufferUid);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @GetMapping("/buffers")
    public ResponseEntity<BuffersListResponse> getBuffersByClient() {
        log.info("Getting all buffers for client");

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<BufferBLM> buffers = bufferService.getBuffersByClient(clientUid);
        List<BufferDTO> bufferDTOs = buffers.stream()
                .map(bufferConverter::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }

    @GetMapping("/buffers/device/{deviceUid}")
    public ResponseEntity<BuffersListResponse> getBuffersByDevice(@PathVariable UUID deviceUid) {
        log.info("Getting buffers for device: {}", deviceUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<BufferBLM> buffers = bufferService.getBuffersByDevice(clientUid, deviceUid);
        List<BufferDTO> bufferDTOs = buffers.stream()
                .map(bufferConverter::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }

    @GetMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<BuffersListResponse> getBuffersByConnectionScheme(@PathVariable UUID connectionSchemeUid) {
        log.info("Getting buffers for connection scheme: {}", connectionSchemeUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<BufferBLM> buffers = bufferService.getBuffersByConnectionScheme(clientUid, connectionSchemeUid);
        List<BufferDTO> bufferDTOs = buffers.stream()
                .map(bufferConverter::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }

    @PutMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferResponse> updateBuffer(
            @PathVariable UUID bufferUid,
            @RequestBody BufferDTO bufferDTO) {

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Updating buffer: {} for client: {}", bufferUid, clientUid);

        bufferValidator.validate(bufferDTO);
        BufferBLM buffer = bufferService.updateBuffer(clientUid, bufferUid, bufferDTO);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @DeleteMapping("/buffers/{bufferUid}")
    public ResponseEntity<Void> deleteBuffer(@PathVariable UUID bufferUid) {
        log.info("Deleting buffer: {}", bufferUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        bufferService.deleteBuffer(clientUid, bufferUid);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/buffers/{bufferUid}/scheme/{connectionSchemeUid}")
    public ResponseEntity<Void> deleteBufferFromConnectionScheme(
            @PathVariable UUID bufferUid,
            @PathVariable UUID connectionSchemeUid) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Removing buffer {} from connection scheme {} for client: {}", 
                bufferUid, connectionSchemeUid, clientUid);
        
        bufferService.deleteBufferFromConnectionScheme(clientUid, bufferUid, connectionSchemeUid);
        
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/buffers/scheme/{connectionSchemeUid}")
    public ResponseEntity<Void> deleteBuffersByConnectionScheme(@PathVariable UUID connectionSchemeUid) {
        log.info("Deleting buffers for connection scheme: {}", connectionSchemeUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        bufferService.deleteAllBuffersFromConnectionScheme(clientUid, connectionSchemeUid);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check: status: OK, service: buffer-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(bufferService.getHealthStatus());
    }
}