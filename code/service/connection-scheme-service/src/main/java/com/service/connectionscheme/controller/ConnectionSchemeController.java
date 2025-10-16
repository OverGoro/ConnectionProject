package com.service.connectionscheme.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.service.connectionscheme.ConnectionSchemeService;
import com.service.connectionscheme.config.SecurityUtils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ConnectionSchemeController {
    @Qualifier("ApiConnectionSchemeService")
    private final ConnectionSchemeService connectionSchemeService;
    private final ConnectionSchemeValidator schemeValidator;
    private final ConnectionSchemeConverter schemeConverter;

    // Константы для пагинации по умолчанию
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 1000;

    @PostMapping("/schemes")
    public ResponseEntity<ConnectionSchemeResponse> createScheme(@RequestBody ConnectionSchemeDTO schemeDTO) {
        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM scheme = connectionSchemeService.createScheme(schemeDTO);

        return ResponseEntity.ok(new ConnectionSchemeResponse(scheme.getUid()));
    }


    @GetMapping("/schemes")
    public ResponseEntity<ConnectionSchemesListResponse> getSchemes(
            @RequestParam(required = false) List<UUID> schemeUids,
            @RequestParam(defaultValue = "" + DEFAULT_OFFSET) int offset,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        List<ConnectionSchemeBLM> schemes = null;

        if (schemeUids != null && !schemeUids.isEmpty()) {
            log.info("Getting specific connection schemes: {} with offset: {}, limit: {}", schemeUids, offset, limit);
            schemes = connectionSchemeService.getSchemeByUid(schemeUids);
        } else {
            // Получение всех схем клиента
            log.info("Getting all connection schemes for client with offset: {}, limit: {}", offset, limit);
            schemes = connectionSchemeService.getSchemesByClient(clientUid);
        }
        
        // Применяем пагинацию
        List<ConnectionSchemeBLM> paginatedSchemes = applyPagination(schemes, offset, limit);
        List<ConnectionSchemeDTO> schemeDTOs = paginatedSchemes.stream()
                .map(schemeConverter::toDTO)
                .collect(Collectors.toList());

        // Создаем информацию о пагинации
        ConnectionSchemesListResponse.PaginationInfo paginationInfo = 
            new ConnectionSchemesListResponse.PaginationInfo(
                offset, 
                limit, 
                schemes.size(), 
                (offset + limit) < schemes.size()
            );

        return ResponseEntity.ok(new ConnectionSchemesListResponse(schemeDTOs, paginationInfo));
    }

    @PutMapping("/schemes/{schemeUid}")
    public ResponseEntity<ConnectionSchemeResponse> updateScheme(
            @PathVariable UUID schemeUid,
            @RequestBody ConnectionSchemeDTO schemeDTO) {
        
        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM scheme = connectionSchemeService.updateScheme(schemeUid, schemeDTO);

        return ResponseEntity.ok(new ConnectionSchemeResponse(scheme.getUid()));
    }

    @DeleteMapping("/schemes")
    public ResponseEntity<Void> deleteSchemes(
            @RequestParam(required = false) List<UUID> schemeUids) {
        
        if (schemeUids != null && !schemeUids.isEmpty()) {
            log.info("Deleting multiple connection schemes: {}", schemeUids);
            for (UUID uid : schemeUids) {
                connectionSchemeService.deleteScheme(uid);
            }
        } else {
            log.warn("No valid delete parameters provided");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info("Health check: status: OK, service: connection-scheme-service, timestamp: {}", 
                System.currentTimeMillis());
                
        return ResponseEntity.ok().body(new HealthResponse(connectionSchemeService.getHealthStatus().toString()));
    }

    /**
     * Применяет пагинацию к списку схем подключения
     * 
     * @param schemes полный список схем подключения
     * @param offset смещение (начальная позиция)
     * @param limit максимальное количество элементов
     * @return пагинированный список схем подключения
     */
    private List<ConnectionSchemeBLM> applyPagination(List<ConnectionSchemeBLM> schemes, int offset, int limit) {
        // Валидация параметров пагинации
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }
        
        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        
        // Применяем пагинацию
        return schemes.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
}