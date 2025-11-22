package com.service.buffer.controller;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDto;
import com.connection.processing.buffer.validator.BufferValidator;
import com.service.buffer.BufferService;
import com.service.buffer.config.SecurityUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

/** . */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buffer")
public class BufferController {

    @Qualifier("ApiBufferService")
    private final BufferService bufferService;
    private final BufferValidator bufferValidator;
    private final BufferConverter bufferConverter;

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 1000;

    /** . */
    @PostMapping("/buffers")
    public ResponseEntity<BufferResponse> createBuffer(
            @RequestBody BufferDto bufferDto) {
        bufferValidator.validate(bufferDto);
        BufferBlm bufferBlm = bufferConverter.toBlm(bufferDto);
        BufferBlm buffer = bufferService.createBuffer(bufferBlm);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    /** . */
    @GetMapping("/buffers")
    public ResponseEntity<BuffersListResponse> getBuffers(
            @RequestParam(required = false) List<UUID> bufferUids,
            @RequestParam(required = false) UUID deviceUid,
            @RequestParam(required = false) UUID connectionSchemeUid,
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<BufferBlm> buffers;

        if (deviceUid != null) {
            log.info(
                    "Getting buffers for device: {} with offset: {}, limit: {}",
                    deviceUid, offset, limit);
            buffers = bufferService.getBuffersByDevice(deviceUid);
        } else if (connectionSchemeUid != null) {
            log.info(
                    "Getting buffers for connection scheme: {} with offset: {}, limit: {}",
                    connectionSchemeUid, offset, limit);
            buffers = bufferService
                    .getBuffersByConnectionScheme(connectionSchemeUid);
        } else {
            log.info(
                    "Getting all buffers for client with offset: {}, limit: {}",
                    offset, limit);
            buffers = bufferService.getBuffersByClient(clientUid);
        }
        BuffersListResponse.PaginationInfo paginationInfo =
                new BuffersListResponse.PaginationInfo(offset, limit,
                        buffers.size(), (offset + limit) < buffers.size());
        // Применяем пагинацию
        List<BufferDto> bufferDtos = applyPagination(buffers, offset, limit)
                .stream().map(bufferConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity
                .ok(new BuffersListResponse(bufferDtos, paginationInfo));
    }

    /** . */
    @PatchMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferResponse> partialUpdateBuffer(
            @PathVariable UUID bufferUid,
            @RequestBody PartialBufferUpdateRequest updateRequest) {

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Partial update buffer: {} for client: {}", bufferUid,
                clientUid);

        // Временная реализация - получаем текущий буфер и возвращаем его
        BufferBlm buffer = bufferService.getBufferByUid(bufferUid);
        buffer.setMaxMessageSize(updateRequest.maxSize);
        buffer.setMaxMessagesNumber(updateRequest.maxMessages);

        bufferValidator.validate(buffer);

        bufferService.updateBuffer(bufferUid, buffer);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    /** . */
    @PutMapping("/buffers/{bufferUid}")
    public ResponseEntity<BufferResponse> updateBuffer(
            @PathVariable UUID bufferUid, @RequestBody BufferDto bufferDto) {

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.info("Updating buffer: {} for client: {}", bufferUid, clientUid);

        bufferValidator.validate(bufferDto);

        BufferBlm bufferBlm = bufferConverter.toBlm(bufferDto);

        BufferBlm buffer = bufferService.updateBuffer(bufferUid, bufferBlm);

        return ResponseEntity.ok(new BufferResponse(buffer.getUid()));
    }

    /** . */
    @DeleteMapping("/buffers")
    public ResponseEntity<Void> deleteBuffers(
            @RequestParam List<UUID> bufferUids) {

        for (UUID b : bufferUids) {
            bufferService.deleteBuffer(b);
        }
        return ResponseEntity.noContent().build();
    }

    /** . */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info(
                "Health check: status: OK, service: buffer-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(
                new HealthResponse(bufferService.getHealthStatus().toString()));
    }

    /**
     * Применяет пагинацию к списку буферов.
     * 
     * @param buffers полный список буферов
     * @param offset смещение (начальная позиция)
     * @param limit максимальное количество элементов
     * @return пагинированный список буферов
     */
    private List<BufferBlm> applyPagination(List<BufferBlm> buffers, int offset,
            int limit) {
        // Валидация параметров пагинации
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }

        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }

        // Применяем пагинацию
        return buffers.stream().skip(offset).limit(limit)
                .collect(Collectors.toList());
    }
    
    /** . */
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
