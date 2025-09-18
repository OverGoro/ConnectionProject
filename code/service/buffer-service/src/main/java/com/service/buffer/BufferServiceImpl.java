// BufferServiceImpl.java
package com.service.buffer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.service.buffer.client.AuthServiceClient;
import com.service.buffer.client.ConnectionSchemeServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class BufferServiceImpl implements BufferService {
    
    private final BufferRepository bufferRepository;
    private final BufferConverter bufferConverter;
    private final BufferValidator bufferValidator;
    private final AuthServiceClient authServiceClient;
    private final ConnectionSchemeServiceClient connectionSchemeServiceClient;

    @Override
    public BufferBLM createBuffer(String accessToken, BufferDTO bufferDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        bufferValidator.validate(bufferDTO);
        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM connectionScheme = connectionSchemeServiceClient.getScheme(
            accessToken, bufferBLM.getConnectionSchemeUid());
        
        if (!clientUid.equals(connectionScheme.getClientUid())) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        // Проверяем, что буфер с таким UID не существует
        if (bufferRepository.exists(bufferBLM.getUid())) {
            throw new BufferAlreadyExistsException("Buffer with UID '" + bufferBLM.getUid() + "' already exists");
        }

        BufferDALM bufferDALM = bufferConverter.toDALM(bufferBLM);
        bufferRepository.add(bufferDALM);
        
        log.info("Buffer created: {} for connection scheme: {}", bufferBLM.getUid(), bufferBLM.getConnectionSchemeUid());
        return bufferBLM;
    }

    @Override
    public BufferBLM getBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        BufferDALM bufferDALM = bufferRepository.findByUid(bufferUid);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM connectionScheme = connectionSchemeServiceClient.getScheme(
            accessToken, bufferDALM.getConnectionSchemeUid());
        
        if (!clientUid.equals(connectionScheme.getClientUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }
        
        return bufferConverter.toBLM(bufferDALM);
    }

    @Override
    public List<BufferBLM> getBuffersByClient(String accessToken) {
        validateTokenAndGetClientUid(accessToken);
        
        // Получаем все схемы подключения клиента
        List<ConnectionSchemeBLM> connectionSchemes = connectionSchemeServiceClient.getSchemesByClient(accessToken);
        
        // Получаем все буферы для всех схем подключения клиента
        return connectionSchemes.stream()
                .flatMap(scheme -> bufferRepository.findByConnectionSchemeUid(scheme.getUid()).stream())
                .map(bufferConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferBLM> getBuffersByConnectionScheme(String accessToken, UUID connectionSchemeUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM connectionScheme = connectionSchemeServiceClient.getScheme(accessToken, connectionSchemeUid);
        
        if (!clientUid.equals(connectionScheme.getClientUid())) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferDALM> buffersDALM = bufferRepository.findByConnectionSchemeUid(connectionSchemeUid);
        return buffersDALM.stream()
                .map(bufferConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public BufferBLM updateBuffer(String accessToken, UUID bufferUid, BufferDTO bufferDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем существование буфера
        BufferDALM existingBuffer = bufferRepository.findByUid(bufferUid);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM existingConnectionScheme = connectionSchemeServiceClient.getScheme(
            accessToken, existingBuffer.getConnectionSchemeUid());
        
        if (!clientUid.equals(existingConnectionScheme.getClientUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        bufferValidator.validate(bufferDTO);
        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);
        
        // Проверяем, что клиент из токена совпадает с клиентом новой схемы
        ConnectionSchemeBLM newConnectionScheme = connectionSchemeServiceClient.getScheme(
            accessToken, bufferBLM.getConnectionSchemeUid());
        
        if (!clientUid.equals(newConnectionScheme.getClientUid())) {
            throw new SecurityException("New connection scheme doesn't belong to the authenticated client");
        }

        // Проверяем, что UID не изменяется
        if (!bufferUid.equals(bufferBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change buffer UID");
        }

        BufferDALM bufferDALM = bufferConverter.toDALM(bufferBLM);
        bufferRepository.update(bufferDALM);
        
        log.info("Buffer updated: {} for connection scheme: {}", bufferUid, bufferBLM.getConnectionSchemeUid());
        return bufferBLM;
    }

    @Override
    public void deleteBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем существование буфера
        BufferDALM existingBuffer = bufferRepository.findByUid(bufferUid);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM connectionScheme = connectionSchemeServiceClient.getScheme(
            accessToken, existingBuffer.getConnectionSchemeUid());
        
        if (!clientUid.equals(connectionScheme.getClientUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        bufferRepository.delete(bufferUid);
        log.info("Buffer deleted: {}", bufferUid);
    }

    @Override
    public void deleteBuffersByConnectionScheme(String accessToken, UUID connectionSchemeUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что схема подключения принадлежит клиенту
        ConnectionSchemeBLM connectionScheme = connectionSchemeServiceClient.getScheme(accessToken, connectionSchemeUid);
        
        if (!clientUid.equals(connectionScheme.getClientUid())) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        bufferRepository.deleteByConnectionSchemeUid(connectionSchemeUid);
        log.info("Buffers deleted for connection scheme: {}", connectionSchemeUid);
    }

    @Override
    public boolean bufferExists(String accessToken, UUID bufferUid) {
        validateToken(accessToken);
        return bufferRepository.exists(bufferUid);
    }

    private UUID validateTokenAndGetClientUid(String accessToken) {
        validateToken(accessToken);
        return authServiceClient.getAccessTokenClientUID(accessToken);
    }

    private void validateToken(String accessToken) {
        authServiceClient.validateAccessToken(accessToken);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        return Map.of(
                "status", "OK",
                "service", "buffer-service",
                "timestamp", System.currentTimeMillis(),
                "auth-service", authServiceClient.healthCheck(),
                "connection-scheme-service", connectionSchemeServiceClient.healthCheck());
    }
}