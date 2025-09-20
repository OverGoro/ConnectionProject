// ConnectionSchemeServiceImpl.java
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
import com.service.connectionscheme.client.AuthServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class ConnectionSchemeServiceImpl implements ConnectionSchemeService {

    private final ConnectionSchemeRepository schemeRepository;
    private final ConnectionSchemeConverter schemeConverter;
    private final ConnectionSchemeValidator schemeValidator;
    private final AuthServiceClient authServiceClient;

    @Override
    public ConnectionSchemeBLM createScheme(String accessToken, ConnectionSchemeDTO schemeDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM schemeBLM = schemeConverter.toBLM(schemeDTO);

        // Проверяем, что клиент из токена совпадает с клиентом схемы
        if (!clientUid.equals(schemeBLM.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        // Проверяем, что схема с таким UID не существует
        if (schemeRepository.exists(schemeBLM.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Scheme with UID '" + schemeBLM.getUid() + "' already exists");
        }

        ConnectionSchemeDALM schemeDALM = schemeConverter.toDALM(schemeBLM);
        schemeRepository.add(schemeDALM);

        log.info("Connection scheme created: {} for client: {}", schemeBLM.getUid(), clientUid);
        return schemeBLM;
    }

    @Override
    public ConnectionSchemeBLM getScheme(String accessToken, UUID schemeUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        ConnectionSchemeDALM schemeDALM = schemeRepository.findByUid(schemeUid);

        // Проверяем, что схема принадлежит клиенту из токена
        if (!clientUid.equals(schemeDALM.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        return schemeConverter.toBLM(schemeDALM);
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByClient(String accessToken) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        List<ConnectionSchemeDALM> schemesDALM = schemeRepository.findByClientUid(clientUid);
        return schemesDALM.stream()
                .map(schemeConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionSchemeBLM updateScheme(String accessToken, UUID schemeUid, ConnectionSchemeDTO schemeDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        // Проверяем существование схемы и принадлежность клиенту
        ConnectionSchemeDALM existingScheme = schemeRepository.findByUid(schemeUid);
        if (!clientUid.equals(existingScheme.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        schemeValidator.validate(schemeDTO);
        ConnectionSchemeBLM schemeBLM = schemeConverter.toBLM(schemeDTO);

        // Проверяем, что клиент из токена совпадает с клиентом схемы
        if (!clientUid.equals(schemeBLM.getClientUid())) {
            throw new SecurityException("Client UID from token doesn't match scheme client UID");
        }

        // Проверяем, что UID не изменяется
        if (!schemeUid.equals(schemeBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change scheme UID");
        }

        ConnectionSchemeDALM schemeDALM = schemeConverter.toDALM(schemeBLM);
        schemeRepository.update(schemeDALM);

        log.info("Connection scheme updated: {} for client: {}", schemeUid, clientUid);
        return schemeBLM;
    }

    @Override
    public void deleteScheme(String accessToken, UUID schemeUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        // Проверяем существование схемы и принадлежность клиенту
        ConnectionSchemeDALM existingScheme = schemeRepository.findByUid(schemeUid);
        if (!clientUid.equals(existingScheme.getClientUid())) {
            throw new SecurityException("Scheme doesn't belong to the authenticated client");
        }

        schemeRepository.delete(schemeUid);
        log.info("Connection scheme deleted: {} for client: {}", schemeUid, clientUid);
    }

    @Override
    public boolean schemeExists(String accessToken, UUID schemeUid) {
        validateToken(accessToken);
        return schemeRepository.exists(schemeUid);
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
        Map<String, Object> authServiceHealth;
        try {
            authServiceHealth = authServiceClient.healthCheck();
        } catch (Exception e) {
            authServiceHealth = Map.of("status", "DOWN");
        }
        return Map.of(
                "status", "OK",
                "service", "connection-scheme-service",
                "timestamp", System.currentTimeMillis(),
                "auth-service: ", authServiceHealth);
    }
}