package com.service.connectionscheme;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.connection.service.auth.AuthService;
import com.service.connectionscheme.config.SecurityUtils;
// import com.service.connectionscheme.kafka.KafkaAuthClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** . */
@Slf4j
@RequiredArgsConstructor
@Service("ApiConnectionSchemeService")
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class ConnectionSchemeServiceImpl implements ConnectionSchemeService {

    private final ConnectionSchemeRepository schemeRepository;
    private final ConnectionSchemeValidator schemeValidator;
    private final AuthService authClient;

    @Override
    public ConnectionSchemeBlm createScheme(ConnectionSchemeBlm schemeBlm) {
        schemeValidator.validate(schemeBlm);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!clientUid.equals(schemeBlm.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        if (schemeRepository.exists(schemeBlm.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Scheme with UID '" + schemeBlm.getUid() + "' already exists");
        }
        log.info("Adding connection scheme: " + schemeBlm.getUid().toString() + " for client "
                + schemeBlm.getClientUid().toString());
        try {
            schemeRepository.add(schemeBlm);
        } catch (ConnectionSchemeAlreadyExistsException e) {
            log.error(e.toString());
            log.error(e.getMessage());
            throw e;
        }
        log.info("Connection scheme created: {} for client: {}", schemeBlm.getUid(), clientUid);
        return schemeBlm;
    }

    @Override
    public ConnectionSchemeBlm getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeBlm schemeBlm = schemeRepository.findByUid(schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        if (!clientUid.equals(schemeBlm.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        return (schemeBlm);
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemeByUid(List<UUID> schemeUids) {
        Set<ConnectionSchemeBlm> connectionSchemeBlms = new HashSet<>();
        for (UUID uuid : schemeUids) {
            connectionSchemeBlms.add(getSchemeByUid(uuid));
        }
        return new ArrayList<ConnectionSchemeBlm>(connectionSchemeBlms);
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemesByClient(UUID clientUid) {
        List<ConnectionSchemeBlm> schemesBlm = schemeRepository.findByClientUid(clientUid);
        return schemesBlm;
    }

    @Override
    public ConnectionSchemeBlm updateScheme(UUID schemeUid, ConnectionSchemeBlm schemeBlm) {
        ConnectionSchemeBlm existingScheme = schemeRepository.findByUid(schemeUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();

        if (!clientUid.equals(existingScheme.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        schemeValidator.validate(schemeBlm);

        // Проверяем, что клиент из токена совпадает с клиентом схемы
        if (!clientUid.equals(schemeBlm.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        // Проверяем, что UID не изменяется
        if (!schemeUid.equals(schemeBlm.getUid())) {
            throw new IllegalArgumentException("Cannot change scheme UID");
        }

        schemeRepository.update(schemeBlm);

        log.info("Connection scheme updated: {} for client: {}", schemeUid, clientUid);
        return schemeBlm;
    }

    @Override
    public void deleteScheme(UUID schemeUid) {
        ConnectionSchemeBlm existingScheme = schemeRepository.findByUid(schemeUid);
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
    public List<ConnectionSchemeBlm> getSchemesByBuffer(UUID bufferUuid) {
        List<ConnectionSchemeBlm> schemesBlm = schemeRepository.findByBufferUid(bufferUuid);
        return schemesBlm;
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemesByBuffer(List<UUID> bufferUuids) {
        Set<ConnectionSchemeBlm> connectionSchemeBlms = new HashSet<>();
        for (UUID uuid : bufferUuids) {
            connectionSchemeBlms.addAll(getSchemesByBuffer(uuid));
        }
        return new ArrayList<ConnectionSchemeBlm>(connectionSchemeBlms);

    }
}