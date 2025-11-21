package com.connection.message.controller;

import com.connection.message.MessageService;
import com.connection.message.converter.MessageConverter;
import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** . */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message")
@Tag(name = "Message Service",
        description = "API для управления сообщениями устройств")
public class MessageController {

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 1;
    // private static final int MAX_LIMIT = 1000;

    protected final MessageConverter messageConverter;
    protected final MessageService messageService;

    /** . */
    @Operation(security = {@SecurityRequirement(name = "clientAuth"),
        @SecurityRequirement(name = "deviceAuth")})
    @PostMapping("/messages")
    public ResponseEntity<Void> addMessage(@Parameter(
            description = "Dto сообщения для добавления") @RequestBody MessageDto messageDto) {

        try {
            // Конвертируем Dto в Blm
            MessageBlm messageBlm = messageConverter.toBlm(messageDto);

            // Вызываем сервис
            messageService.addMessage(messageBlm);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error adding message: {}", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /** . */
    @Operation(
            security = {@SecurityRequirement(name = "clientAuth"),
                @SecurityRequirement(name = "deviceAuth")},
            parameters = {
                @Parameter(name = "schemeUids",
                        description = "Список UID схем соединения",
                        in = ParameterIn.QUERY),
                @Parameter(name = "bufferUids", description = "Список UID буферов",
                        in = ParameterIn.QUERY),
                @Parameter(name = "deviceUids",
                        description = "Список UID устройств",
                        in = ParameterIn.QUERY),
                @Parameter(name = "deleteOnGet",
                        description = "Удалять сообщения после получения",
                        in = ParameterIn.QUERY),
                @Parameter(name = "offset", description = "Смещение для пагинации",
                        in = ParameterIn.QUERY),
                @Parameter(name = "limit",
                        description = "Лимит для пагинации (макс. 1000)",
                        in = ParameterIn.QUERY)})
    @GetMapping("/messages/")
    public ResponseEntity<MessageResponse> getMessage(
            @RequestParam(required = false) List<UUID> schemeUids,
            @RequestParam(required = false) List<UUID> bufferUids,
            @RequestParam(required = false) List<UUID> deviceUids,
            @RequestParam(defaultValue = "false") Boolean deleteOnGet,
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {

        Set<MessageBlm> messageBlms = new HashSet<>();
        log.info("Getting messages");

        if (schemeUids != null) {
            for (UUID schemeUid : schemeUids) {
                log.info("Getting messages for schemeUid: {}", schemeUid);
                messageBlms.addAll(messageService.getMessagesByScheme(schemeUid,
                        deleteOnGet, offset, limit));
            }
        }

        if (bufferUids != null) {
            for (UUID bufferUid : bufferUids) {
                log.info("Getting messages for bufferUid: {}", bufferUid);
                messageBlms.addAll(messageService.getMessagesByBuffer(bufferUid,
                        deleteOnGet, offset, limit));
            }
        }

        if (deviceUids != null) {
            for (UUID deviceUid : deviceUids) {
                log.info("Getting messages for deviceUid: {}", deviceUid);
                messageBlms.addAll(messageService.getMessagesByDevice(deviceUid,
                        deleteOnGet, offset, limit));
            }
        }
        log.info("Got messages: {}", messageBlms.size());

        List<MessageBlm> resultBlm = new ArrayList<>(messageBlms);
        List<MessageDto> resultDto =
                resultBlm.stream().map(messageConverter::toDto).toList();
        return ResponseEntity.ok().body(new MessageResponse(resultDto));
    }

    /** . */
    @Operation(summary = "Health Check",
            description = "Проверка статуса сервиса и зависимостей. Не требует аутентификации.")
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: service: message-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok()
                .body(new HealthResponse(messageService.health().toString()));
    }
}
