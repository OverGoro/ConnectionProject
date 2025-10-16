package com.connection.message.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.message.MessageService;
import com.connection.message.converter.MessageConverter;
import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MessageController {
    
    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 1;
    private static final int MAX_LIMIT = 1000;

    protected MessageConverter messageConverter;
    protected MessageService messageService;

    @PostMapping("/messages")
    public ResponseEntity<Void> addMessage(
            @RequestBody MessageDTO messageDTO) {
        throw new UnsupportedOperationException("Unimplemented method 'addMessage'");
    }

    @GetMapping("/messages/")
    public ResponseEntity<MessageResponse> getMessage(
            @RequestParam(required = false) List<UUID> schemeUids,
            @RequestParam(required = false) List<UUID> bufferUids,
            @RequestParam(required = false) List<UUID> deviceUids,
            @RequestParam(defaultValue = "false") Boolean deleteOnGet,
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        Set<MessageBLM> messageBLMs = new HashSet<>();
        log.info("Getting messages");

        if (schemeUids != null){
            for (UUID schemeUid : schemeUids){
                log.info("Getting messages for schemeUid: {}", schemeUid);
                messageBLMs.addAll(messageService.getMessagesByScheme(schemeUid, deleteOnGet, offset, limit));
            }
        }

        if (bufferUids != null){
            for (UUID bufferUid : bufferUids){
                log.info("Getting messages for bufferUid: {}", bufferUid);
                messageBLMs.addAll(messageService.getMessagesByScheme(bufferUid, deleteOnGet, offset, limit));
            }
        }

        if (deviceUids != null){
            for (UUID deviceUid : deviceUids){
                log.info("Getting messages for deviceUid: {}", deviceUid);
                messageBLMs.addAll(messageService.getMessagesByScheme(deviceUid, deleteOnGet, offset, limit));
            }
        }
        log.info("Got messages: {}", messageBLMs.size());

        List<MessageBLM> resultBLM = new ArrayList<>(messageBLMs);
        List<MessageDTO> resultDTO = resultBLM.stream().map(messageConverter::toDTO).toList();
        return ResponseEntity.ok().body( new MessageResponse(resultDTO));
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: service: message-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(messageService.health().toString()));

    }
}