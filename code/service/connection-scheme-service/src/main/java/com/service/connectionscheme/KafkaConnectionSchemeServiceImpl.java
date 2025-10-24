package com.service.connectionscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.service.connectionscheme.kafka.TypedAuthKafkaClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("KafkaConnectionSchemeService")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class KafkaConnectionSchemeServiceImpl implements ConnectionSchemeService {

    private final ConnectionSchemeRepository schemeRepository;
    private final ConnectionSchemeValidator schemeValidator;

    private final TypedAuthKafkaClient authKafkaClient;

    @Override
    public ConnectionSchemeBLM createScheme(ConnectionSchemeBLM schemeBLM) {
        schemeValidator.validate(schemeBLM);

        if (schemeRepository.exists(schemeBLM.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Scheme with UID '" + schemeBLM.getUid() + "' already exists");
        }

        schemeRepository.add(schemeBLM);

        log.info("Connection scheme created via Kafka: {}", schemeBLM.getUid());
        return schemeBLM;
    }

    @Override
    public ConnectionSchemeBLM getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeBLM schemeBLM = schemeRepository.findByUid(schemeUid);
        return (schemeBLM); // Без проверки безопасности
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemeByUid(List<UUID> schemeUids) {
        return schemeUids.stream()
                .map(this::getSchemeByUid)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUid) {
        List<ConnectionSchemeBLM> schemesBLM = schemeRepository.findByClientUid(clientUid);
        return schemesBLM;
    }

    @Override
    public ConnectionSchemeBLM updateScheme(UUID schemeUid, ConnectionSchemeBLM schemeBLM) {
        schemeRepository.findByUid(schemeUid);
        
        schemeValidator.validate(schemeBLM);

        // Проверяем, что UID не изменяется
        if (!schemeUid.equals(schemeBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change scheme UID");
        }

        schemeRepository.update(schemeBLM);

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
        List<ConnectionSchemeBLM> schemesBLM = schemeRepository.findByBufferUid(bufferUuid);
        return schemesBLM;
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByBuffer(List<UUID> bufferUuids) {
        return bufferUuids.stream()
                .flatMap(uuid -> getSchemesByBuffer(uuid).stream())
                .collect(Collectors.toList());
    }
}