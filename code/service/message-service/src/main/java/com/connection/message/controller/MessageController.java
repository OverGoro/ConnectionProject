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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message")
@Tag(name = "Message Service", description = "API для управления сообщениями устройств")
public class MessageController {

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 1;
    private static final int MAX_LIMIT = 1000;

    protected final MessageConverter messageConverter;
    protected final MessageService messageService;

    @Operation(security = {
            @SecurityRequirement(name = "clientAuth"),
            @SecurityRequirement(name = "deviceAuth")
    })
    @PostMapping("/messages")
    public ResponseEntity<Void> addMessage(
            @Parameter(description = "DTO сообщения для добавления") @RequestBody MessageDTO messageDTO) {

        try {
            // Конвертируем DTO в BLM
            MessageBLM messageBLM = messageConverter.toBLM(messageDTO);

            // Вызываем сервис
            messageService.addMessage(messageBLM);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error adding message: {}", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(security = {
            @SecurityRequirement(name = "clientAuth"),
            @SecurityRequirement(name = "deviceAuth")
    }, parameters = {
            @Parameter(name = "schemeUids", description = "Список UID схем соединения", in = ParameterIn.QUERY),
            @Parameter(name = "bufferUids", description = "Список UID буферов", in = ParameterIn.QUERY),
            @Parameter(name = "deviceUids", description = "Список UID устройств", in = ParameterIn.QUERY),
            @Parameter(name = "deleteOnGet", description = "Удалять сообщения после получения", in = ParameterIn.QUERY),
            @Parameter(name = "offset", description = "Смещение для пагинации", in = ParameterIn.QUERY),
            @Parameter(name = "limit", description = "Лимит для пагинации (макс. 1000)", in = ParameterIn.QUERY)
    })
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

        if (schemeUids != null) {
            for (UUID schemeUid : schemeUids) {
                log.info("Getting messages for schemeUid: {}", schemeUid);
                messageBLMs.addAll(messageService.getMessagesByScheme(schemeUid, deleteOnGet, offset, limit));
            }
        }

        if (bufferUids != null) {
            for (UUID bufferUid : bufferUids) {
                log.info("Getting messages for bufferUid: {}", bufferUid);
                messageBLMs.addAll(messageService.getMessagesByBuffer(bufferUid, deleteOnGet, offset, limit));
            }
        }

        if (deviceUids != null) {
            for (UUID deviceUid : deviceUids) {
                log.info("Getting messages for deviceUid: {}", deviceUid);
                messageBLMs.addAll(messageService.getMessagesByDevice(deviceUid, deleteOnGet, offset, limit));
            }
        }
        log.info("Got messages: {}", messageBLMs.size());

        List<MessageBLM> resultBLM = new ArrayList<>(messageBLMs);
        List<MessageDTO> resultDTO = resultBLM.stream().map(messageConverter::toDTO).toList();
        return ResponseEntity.ok().body(new MessageResponse(resultDTO));
    }

    @Operation(summary = "Health Check", description = "Проверка статуса сервиса и зависимостей. Не требует аутентификации.")
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: service: message-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(messageService.health().toString()));
    }
}