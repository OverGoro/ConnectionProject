// BufferJsonDataServiceImpl.java
package com.connection.bufferjsondata;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.processing.buffer.objects.json.converter.BufferJsonDataConverter;
import com.connection.processing.buffer.objects.json.exception.BufferJsonDataAlreadyExistsException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;
import com.connection.processing.buffer.objects.json.repository.BufferJsonDataRepository;
import com.connection.processing.buffer.objects.json.validator.BufferJsonDataValidator;
import com.connection.bufferjsondata.client.AuthServiceClient;
import com.connection.bufferjsondata.client.BufferServiceClient;
import com.connection.processing.buffer.model.BufferBLM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class BufferJsonDataServiceImpl implements BufferJsonDataService {
    
    private final BufferJsonDataRepository jsonDataRepository;
    private final BufferJsonDataConverter jsonDataConverter;
    private final BufferJsonDataValidator jsonDataValidator;
    private final AuthServiceClient authServiceClient;
    private final BufferServiceClient bufferServiceClient;

    @Override
    public BufferJsonDataBLM addJsonData(String accessToken, BufferJsonDataDTO jsonDataDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        jsonDataValidator.validate(jsonDataDTO);
        BufferJsonDataBLM jsonDataBLM = jsonDataConverter.toBLM(jsonDataDTO);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, jsonDataBLM.getBufferUid());
        
        // Проверяем, что схема подключения буфера принадлежит клиенту (через буфер сервис)
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        // Проверяем, что JSON данные с таким UID не существуют
        if (jsonDataRepository.exists(jsonDataBLM.getUid())) {
            throw new BufferJsonDataAlreadyExistsException("JSON data with UID '" + jsonDataBLM.getUid() + "' already exists");
        }

        BufferJsonDataDALM jsonDataDALM = jsonDataConverter.toDALM(jsonDataBLM);
        jsonDataRepository.add(jsonDataDALM);
        
        log.info("JSON data added to buffer: {}, data UID: {}", jsonDataBLM.getBufferUid(), jsonDataBLM.getUid());
        return jsonDataBLM;
    }

    @Override
    public BufferJsonDataBLM getJsonData(String accessToken, UUID dataUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        BufferJsonDataDALM jsonDataDALM = jsonDataRepository.findByUid(dataUid);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, jsonDataDALM.getBufferUid());
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("JSON data doesn't belong to the authenticated client");
        }
        
        return jsonDataConverter.toBLM(jsonDataDALM);
    }

    @Override
    public List<BufferJsonDataBLM> getJsonDataByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findByBufferUid(bufferUid);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedAfter(String accessToken, UUID bufferUid, Instant createdAfter) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findByBufferUidAndCreatedAfter(bufferUid, createdAfter);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedBefore(String accessToken, UUID bufferUid, Instant createdBefore) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findByBufferUidAndCreatedBefore(bufferUid, createdBefore);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedBetween(String accessToken, UUID bufferUid, Instant startDate, Instant endDate) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findByBufferUidAndCreatedBetween(bufferUid, startDate, endDate);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public BufferJsonDataBLM getNewestJsonDataByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        BufferJsonDataDALM jsonDataDALM = jsonDataRepository.findNewestByBufferUid(bufferUid);
        return jsonDataConverter.toBLM(jsonDataDALM);
    }

    @Override
    public BufferJsonDataBLM getOldestJsonDataByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        BufferJsonDataDALM jsonDataDALM = jsonDataRepository.findOldestByBufferUid(bufferUid);
        return jsonDataConverter.toBLM(jsonDataDALM);
    }

    @Override
    public List<BufferJsonDataBLM> getNewestJsonDataByBuffer(String accessToken, UUID bufferUid, int limit) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findNewestByBufferUid(bufferUid, limit);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferJsonDataBLM> getOldestJsonDataByBuffer(String accessToken, UUID bufferUid, int limit) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<BufferJsonDataDALM> jsonDataDALM = jsonDataRepository.findOldestByBufferUid(bufferUid, limit);
        return jsonDataDALM.stream()
                .map(jsonDataConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteJsonData(String accessToken, UUID dataUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем существование данных
        BufferJsonDataDALM existingData = jsonDataRepository.findByUid(dataUid);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, existingData.getBufferUid());
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("JSON data doesn't belong to the authenticated client");
        }

        jsonDataRepository.delete(dataUid);
        log.info("JSON data deleted: {}", dataUid);
    }

    @Override
    public void deleteJsonDataByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        jsonDataRepository.deleteByBufferUid(bufferUid);
        log.info("All JSON data deleted for buffer: {}", bufferUid);
    }

    @Override
    public void deleteOldJsonData(String accessToken, Instant olderThan) {
        validateToken(accessToken);
        jsonDataRepository.deleteOldData(olderThan);
        log.info("Old JSON data deleted (older than: {})", olderThan);
    }

    @Override
    public int countJsonDataByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(getClientUidFromBuffer(buffer))) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        return jsonDataRepository.countByBufferUid(bufferUid);
    }

    @Override
    public boolean jsonDataExists(String accessToken, UUID dataUid) {
        validateToken(accessToken);
        return jsonDataRepository.exists(dataUid);
    }

    private UUID getClientUidFromBuffer(BufferBLM buffer) {
        // Для получения clientUid из буфера нужно обратиться к сервису схем подключения
        // через буфер сервис, но в текущей архитектуре это сложно
        // Временно возвращаем null, предполагая что проверка прав будет через буфер сервис
        return null;
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
                "service", "buffer-json-data-service",
                "timestamp", System.currentTimeMillis(),
                "auth-service", authServiceClient.healthCheck(),
                "buffer-service", bufferServiceClient.healthCheck());
    }
}