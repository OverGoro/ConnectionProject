package com.service.buffer.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.scheme.model.ConnectionSchemeBlm;
import com.service.connectionscheme.ConnectionSchemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestConnectionSchemeService implements ConnectionSchemeService {
    // Хранилище тестовых данных
    private final Map<UUID, ConnectionSchemeBlm> testSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBlm>> clientSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBlm>> bufferSchemes = new ConcurrentHashMap<>();


    public void addTestConnectionScheme(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers) {
        ConnectionSchemeBlm scheme = createTestConnectionSchemeBlm(schemeUid, clientUid, usedBuffers);
        addTestConnectionScheme(scheme);
    }

    public void addTestConnectionScheme(ConnectionSchemeBlm scheme) {
        UUID schemeUid = (scheme.getUid());
        UUID clientUid = (scheme.getClientUid());

        testSchemes.put(schemeUid, scheme);

        // Добавляем в список схем клиента
        List<ConnectionSchemeBlm> clientSchemeList = clientSchemes.computeIfAbsent(
                clientUid, k -> new ArrayList<>());
        clientSchemeList.add(scheme);

        // Автоматически создаем связи с буферами из usedBuffers
        if (scheme.getUsedBuffers() != null) {
            for (UUID bufferUid : scheme.getUsedBuffers()) {
                linkSchemeToBuffer(schemeUid, bufferUid);
            }
        }

        log.info("📝 Test Responder: Added connection scheme {} for client {} with {} used buffers",
                schemeUid, clientUid,
                scheme.getUsedBuffers() != null ? scheme.getUsedBuffers().size() : 0);
    }

    public void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeBlm> bufferSchemeList = bufferSchemes.computeIfAbsent(
                    bufferUid, k -> new ArrayList<>());
            if (!bufferSchemeList.contains(scheme)) {
                bufferSchemeList.add(scheme);
            }
            log.info("🔗 Test Responder: Linked scheme {} to buffer {}", schemeUid, bufferUid);
        } else {
            log.warn("⚠️ Test Responder: Cannot link - scheme {} not found", schemeUid);
        }
    }

    public void removeTestConnectionScheme(UUID schemeUid) {
        ConnectionSchemeBlm scheme = testSchemes.remove(schemeUid);
        if (scheme != null) {
            UUID clientUid = (scheme.getClientUid());

            // Удаляем из списка клиента
            List<ConnectionSchemeBlm> clientSchemesList = clientSchemes.get(clientUid);
            if (clientSchemesList != null) {
                clientSchemesList.removeIf(s -> s.getUid().equals(schemeUid.toString()));
            }

            // Удаляем из всех связей с буферами
            bufferSchemes.values()
                    .forEach(schemeList -> schemeList.removeIf(s -> s.getUid().equals(schemeUid.toString())));
        }
    }

    public void clearTestData() {
        testSchemes.clear();
        clientSchemes.clear();
        bufferSchemes.clear();
        log.info("🧹 Test Responder: All connection scheme test data cleared");
    }

    public boolean hasConnectionScheme(UUID schemeUid) {
        return testSchemes.containsKey(schemeUid);
    }

    public boolean connectionSchemeBelongsToClient(UUID schemeUid, UUID clientUid) {
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        return scheme != null && scheme.getClientUid().equals(clientUid.toString());
    }

    private ConnectionSchemeBlm createTestConnectionSchemeBlm(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers) {
        return ConnectionSchemeBlm.builder()
                .uid(schemeUid)
                .clientUid(clientUid)
                .usedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>())
                .schemeJson("{\"test\": true, \"schemeType\": \"integration-test\", \"buffers\": " +
                        (usedBuffers != null ? usedBuffers.toString() : "[]") + "}")
                .build();
    }

    // Дополнительные методы для удобства

    public void addTestConnectionSchemeWithBuffers(UUID schemeUid, UUID clientUid, UUID... bufferUids) {
        List<UUID> usedBuffers = bufferUids != null ? Arrays.asList(bufferUids) : new ArrayList<>();
        addTestConnectionScheme(schemeUid, clientUid, usedBuffers);
    }

    public void addBufferToScheme(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<UUID> usedBuffers = scheme.getUsedBuffers();
            if (usedBuffers == null) {
                usedBuffers = new ArrayList<>();
                scheme.setUsedBuffers(usedBuffers);
            }
            if (!usedBuffers.contains(bufferUid)) {
                usedBuffers.add(bufferUid);
            }
            linkSchemeToBuffer(schemeUid, bufferUid);
            log.info("Test Responder: Added buffer {} to scheme {}", bufferUid, schemeUid);
        }
    }

    public void removeBufferFromScheme(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme != null && scheme.getUsedBuffers() != null) {
            scheme.getUsedBuffers().remove(bufferUid);

            // Удаляем связь
            List<ConnectionSchemeBlm> bufferSchemesList = bufferSchemes.get(bufferUid);
            if (bufferSchemesList != null) {
                bufferSchemesList.removeIf(s -> s.getUid().equals(schemeUid.toString()));
            }
            log.info("Test Responder: Removed buffer {} from scheme {}", bufferUid, schemeUid);
        }
    }


    @Override
    public ConnectionSchemeBlm createScheme(ConnectionSchemeBlm schemeBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createScheme'");
    }


    @Override
    public void deleteScheme(UUID schemeUid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteScheme'");
    }


    @Override
    public Map<String, Object> getHealthStatus() {
        return Map.of("status", "OK");
    }


    @Override
    public ConnectionSchemeBlm getSchemeByUid(UUID schemeUid) {
        return testSchemes.get(schemeUid);
    }


    @Override
    public List<ConnectionSchemeBlm> getSchemeByUid(List<UUID> schemeUid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchemeByUid'");
    }


    @Override
    public List<ConnectionSchemeBlm> getSchemesByBuffer(UUID bufferUuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchemesByBuffer'");
    }


    @Override
    public List<ConnectionSchemeBlm> getSchemesByBuffer(List<UUID> bufferUuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchemesByBuffer'");
    }


    @Override
    public List<ConnectionSchemeBlm> getSchemesByClient(UUID clientUuid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchemesByClient'");
    }


    @Override
    public boolean schemeExists(UUID schemeUid) {
        // TODO Auto-generated method stub
        return testSchemes.get(schemeUid) != null;
    }


    @Override
    public ConnectionSchemeBlm updateScheme(UUID schemeUid, ConnectionSchemeBlm schemeBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateScheme'");
    }
}