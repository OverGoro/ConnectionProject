package com.service.connectionscheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.connection.service.auth.AuthService;
import com.service.connectionscheme.config.SecurityUtils;
// import com.service.connectionscheme.kafka.KafkaAuthClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("ApiConnectionSchemeService")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
//@Transaction("atomicosTransactionManager")
public class ConnectionSchemeServiceImpl implements ConnectionSchemeService {

    private final ConnectionSchemeRepository schemeRepository;
    private final ConnectionSchemeValidator schemeValidator;
    private final AuthService authClient;

    @Override
    public ConnectionSchemeBLM createScheme(ConnectionSchemeBLM schemeBLM) {
        schemeValidator.validate(schemeBLM);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!clientUid.equals(schemeBLM.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        if (schemeRepository.exists(schemeBLM.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Scheme with UID '" + schemeBLM.getUid() + "' already exists");
        }
        log.info("Adding connection scheme: " + schemeBLM.getUid().toString() + " for client "
                + schemeBLM.getClientUid().toString());
        try {
            schemeRepository.add(schemeBLM);
        } catch (ConnectionSchemeAlreadyExistsException e) {
            log.error(e.toString());
            log.error(e.getMessage());
            throw e;
        }
        log.info("Connection scheme created: {} for client: {}", schemeBLM.getUid(), clientUid);
        return schemeBLM;
    }

    @Override
    public ConnectionSchemeBLM getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeBLM schemeBLM = schemeRepository.findByUid(schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        if (!clientUid.equals(schemeBLM.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        return (schemeBLM);
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemeByUid(List<UUID> schemeUids) {
        Set<ConnectionSchemeBLM> connectionSchemeBLMs = new HashSet<>();
        for (UUID uuid : schemeUids) {
            connectionSchemeBLMs.add(getSchemeByUid(uuid));
        }
        return new ArrayList<ConnectionSchemeBLM>(connectionSchemeBLMs);
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUid) {
        List<ConnectionSchemeBLM> schemesBLM = schemeRepository.findByClientUid(clientUid);
        return schemesBLM;
    }

    @Override
    public ConnectionSchemeBLM updateScheme(UUID schemeUid, ConnectionSchemeBLM schemeBLM) {
        ConnectionSchemeBLM existingScheme = schemeRepository.findByUid(schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        if (!clientUid.equals(existingScheme.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        schemeValidator.validate(schemeBLM);

        // Проверяем, что клиент из токена совпадает с клиентом схемы
        if (!clientUid.equals(schemeBLM.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        // Проверяем, что UID не изменяется
        if (!schemeUid.equals(schemeBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change scheme UID");
        }

        schemeRepository.update(schemeBLM);

        log.info("Connection scheme updated: {} for client: {}", schemeUid, clientUid);
        return schemeBLM;
    }

    @Override
    public void deleteScheme(UUID schemeUid) {
        ConnectionSchemeBLM existingScheme = schemeRepository.findByUid(schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        if (!clientUid.equals(existingScheme.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        schemeRepository.delete(schemeUid);
        log.info("Connection scheme deleted: {} for client: {}", schemeUid, clientUid);
    }

    @Override
    public boolean schemeExists(UUID schemeUid) {
        return schemeRepository.exists(schemeUid);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        try {
            // var authHealth = authClient.healthCheck("connection-scheme-service")
            //         .get(5, java.util.concurrent.TimeUnit.SECONDS);
            var authHealth = authClient.getHealthStatus();

            return Map.of(
                    "status", "OK",
                    "service", "connection-scheme-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth != null ? authHealth : "UNAVAILABLE");
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
        Set<ConnectionSchemeBLM> connectionSchemeBLMs = new HashSet<>();
        for (UUID uuid : bufferUuids) {
            connectionSchemeBLMs.addAll(getSchemesByBuffer(uuid));
        }
        return new ArrayList<ConnectionSchemeBLM>(connectionSchemeBLMs);

    }
}