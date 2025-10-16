package com.service.connectionscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.service.connectionscheme.kafka.TypedAuthKafkaClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("kafkaConnectionSchemeService")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class KafkaConnectionSchemeServiceImpl implements ConnectionSchemeService {

    private final ConnectionSchemeRepository schemeRepository;
    private final ConnectionSchemeConverter schemeConverter;
    private final ConnectionSchemeValidator schemeValidator;

    private final TypedAuthKafkaClient authKafkaClient;

    @Override
    public ConnectionSchemeBLM createScheme(ConnectionSchemeDTO schemeDTO) {
        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM schemeBLM = schemeConverter.toBLM(schemeDTO);

        if (schemeRepository.exists(schemeBLM.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Scheme with UID '" + schemeBLM.getUid() + "' already exists");
        }

        ConnectionSchemeDALM schemeDALM = schemeConverter.toDALM(schemeBLM);
        schemeRepository.add(schemeDALM);

        log.info("Connection scheme created via Kafka: {}", schemeBLM.getUid());
        return schemeBLM;
    }

    @Override
    public ConnectionSchemeBLM getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeDALM schemeDALM = schemeRepository.findByUid(schemeUid);
        return schemeConverter.toBLM(schemeDALM); // Без проверки безопасности
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemeByUid(List<UUID> schemeUids) {
        return schemeUids.stream()
                .map(this::getSchemeByUid)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUid) {
        List<ConnectionSchemeDALM> schemesDALM = schemeRepository.findByClientUid(clientUid);
        return schemesDALM.stream()
                .map(schemeConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionSchemeBLM updateScheme(UUID schemeUid, ConnectionSchemeDTO schemeDTO) {
        schemeRepository.findByUid(schemeUid);
        
        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM schemeBLM = schemeConverter.toBLM(schemeDTO);

        // Проверяем, что UID не изменяется
        if (!schemeUid.equals(schemeBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change scheme UID");
        }

        ConnectionSchemeDALM schemeDALM = schemeConverter.toDALM(schemeBLM);
        schemeRepository.update(schemeDALM);

        log.info("Connection scheme updated via Kafka: {}", schemeUid);
        return schemeBLM;
    }

    @Override
    public void deleteScheme(UUID schemeUid) {
        schemeRepository.delete(schemeUid);
        log.info("Connection scheme deleted via Kafka: {}", schemeUid);
    }

    @Override
    public boolean schemeExists(UUID schemeUid) {
        return schemeRepository.exists(schemeUid);
    }

     @Override
    public Map<String, Object> getHealthStatus() {
        try {
            var authHealth = authKafkaClient.healthCheck("connection-scheme-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            return Map.of(
                    "status", "OK",
                    "service", "connection-scheme-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth.isSuccess() ? authHealth.getHealthStatus() : "UNAVAILABLE");
        } catch (Exception e) {
            log.error("Kafka Client: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "connection-scheme-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", "UNAVAILABLE",
                    "error", e.getMessage());
        }
    }
    @Override
    public List<ConnectionSchemeBLM> getSchemesByBuffer(UUID bufferUuid) {
        List<ConnectionSchemeDALM> schemesDALM = schemeRepository.findByBufferUid(bufferUuid);
        return schemesDALM.stream()
                .map(schemeConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByBuffer(List<UUID> bufferUuids) {
        return bufferUuids.stream()
                .flatMap(uuid -> getSchemesByBuffer(uuid).stream())
                .collect(Collectors.toList());
    }
}