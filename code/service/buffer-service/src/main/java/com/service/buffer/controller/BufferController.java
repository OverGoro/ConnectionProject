package com.service.buffer.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/api/v1")
public class BufferController {

    private final BufferService bufferService;
    private final BufferValidator bufferValidator;
    private final BufferConverter bufferConverter;

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 1000;

    @PostMapping("/buffers")
    public ResponseEntity<BufferResponse> createBuffer(@RequestBody BufferDTO bufferDTO) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Creating buffer for client {}", clientUid);

        bufferValidator.validate(bufferDTO);
        BufferBLM buffer = bufferService.createBuffer(clientUid, bufferDTO);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    @GetMapping("/buffers")
    public ResponseEntity<BuffersListResponse> getBuffers(
            @RequestParam(required = false) List<UUID> bufferUids,
            @RequestParam(required = false) UUID deviceUid,
            @RequestParam(required = false) UUID connectionSchemeUid,
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<BufferBLM> buffers;

        if (deviceUid != null) {
            log.info("Getting buffers for device: {} with offset: {}, limit: {}", deviceUid, offset, limit);
            buffers = bufferService.getBuffersByDevice(clientUid, deviceUid);
        } else if (connectionSchemeUid != null) {
            log.info("Getting buffers for connection scheme: {} with offset: {}, limit: {}", 
                    connectionSchemeUid, offset, limit);
            buffers = bufferService.getBuffersByConnectionScheme(clientUid, connectionSchemeUid);
        } else {
            log.info("Getting all buffers for client with offset: {}, limit: {}", offset, limit);
            buffers = bufferService.getBuffersByClient(clientUid);
        }
        
        // Применяем пагинацию
        List<BufferDTO> bufferDTOs = applyPagination(buffers, offset, limit)
                .stream()
                .map(bufferConverter::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new BuffersListResponse(bufferDTOs));
    }
    @PatchMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferResponse> partialUpdateBuffer(
            @PathVariable UUID bufferUid,
            @RequestBody PartialBufferUpdateRequest updateRequest) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Partial update buffer: {} for client: {}", bufferUid, clientUid);
        
        // Временная реализация - получаем текущий буфер и возвращаем его
        BufferBLM buffer = bufferService.getBufferByUid(clientUid, bufferUid);
        
        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
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

    @DeleteMapping("/buffers")
    public ResponseEntity<Void> deleteBuffers(
            @RequestParam List<UUID> bufferUids) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        for (UUID b : bufferUids){
            bufferService.deleteBuffer(clientUid, b);
        }

        // if (bufferUids != null && !bufferUids.isEmpty()) {
        //     // Удаление массива буферов
        //     log.info("Deleting multiple buffers: {} for client: {}", bufferUids, clientUid);
        //     for (UUID uid : bufferUids) {
        //         bufferService.deleteBuffer(clientUid, uid);
        //     }
        // } else if (connectionSchemeUid != null && bufferUid != null) {
        //     // Удаление конкретного буфера из схемы подключения
        //     log.info("Removing buffer {} from connection scheme {} for client: {}", 
        //             bufferUid, connectionSchemeUid, clientUid);
        //     bufferService.deleteBufferFromConnectionScheme(clientUid, bufferUid, connectionSchemeUid);
        // } else if (connectionSchemeUid != null) {
        //     // Удаление всех буферов из схемы подключения
        //     log.info("Deleting all buffers for connection scheme: {} for client: {}", 
        //             connectionSchemeUid, clientUid);
        //     bufferService.deleteAllBuffersFromConnectionScheme(clientUid, connectionSchemeUid);
        // } else if (bufferUid != null) {
        //     // Удаление одного буфера (обратная совместимость)
        //     log.info("Deleting buffer: {} for client: {}", bufferUid, clientUid);
        //     bufferService.deleteBuffer(clientUid, bufferUid);
        // } else {
        //     log.warn("No valid delete parameters provided");
        //     return ResponseEntity.badRequest().build();
        // }

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: status: OK, service: buffer-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(bufferService.getHealthStatus().toString()));
    }

    /**
     * Применяет пагинацию к списку буферов
     * 
     * @param buffers полный список буферов
     * @param offset смещение (начальная позиция)
     * @param limit максимальное количество элементов
     * @return пагинированный список буферов
     */
    private List<BufferBLM> applyPagination(List<BufferBLM> buffers, int offset, int limit) {
        // Валидация параметров пагинации
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }
        
        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        
        // Применяем пагинацию
        return buffers.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
        public static class PartialBufferUpdateRequest {
        private Integer maxMessages;
        private Integer maxSize;

        // Getters and Setters
        public Integer getMaxMessages() {
            return maxMessages;
        }

        public void setMaxMessages(Integer maxMessages) {
            this.maxMessages = maxMessages;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

    }
}