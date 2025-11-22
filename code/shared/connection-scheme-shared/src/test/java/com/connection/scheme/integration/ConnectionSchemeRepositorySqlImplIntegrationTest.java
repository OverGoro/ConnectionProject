package com.connection.scheme.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.repository.ConnectionSchemeRepositorySqlImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Connection Scheme Repository Sql Implementation Tests")
public class ConnectionSchemeRepositorySqlImplIntegrationTest extends BaseConnectionSchemeRepositoryIntegrationTest {

    private ConnectionSchemeRepository connectionSchemeRepository;

    @BeforeEach
    void setUpRepository() {
        this.connectionSchemeRepository = new ConnectionSchemeRepositorySqlImpl(jdbcTemplate);
    }

    @Test
    @DisplayName("Should add connection scheme successfully")
    void shouldAddConnectionSchemeSuccessfully() {
        // Given
        createTestClientInDatabase();
        createTestBuffersInDatabase();

        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(testBufferUid1, Arrays.asList(testBufferUid2));
        bufferTransitions.put(testBufferUid2, Arrays.asList(testBufferUid3));

        ConnectionSchemeBlm testScheme = ConnectionSchemeBlm.builder()
                .uid(testSchemeUid)
                .clientUid(testClientUid)
                .schemeJson(convertTransitionsToJson(bufferTransitions))
                .usedBuffers(Arrays.asList(testBufferUid1, testBufferUid2, testBufferUid3))
                .bufferTransitions(bufferTransitions)
                .build();

        // When
        connectionSchemeRepository.add(testScheme);

        // Then
        ConnectionSchemeBlm foundScheme = connectionSchemeRepository.findByUid(testSchemeUid);

        assertThat(foundScheme).isNotNull();
        assertThat(foundScheme.getUid()).isEqualTo(testSchemeUid);
        assertThat(foundScheme.getClientUid()).isEqualTo(testClientUid);
        assertThat(foundScheme.getUsedBuffers()).hasSize(3);
        assertThat(foundScheme.getUsedBuffers()).contains(testBufferUid1, testBufferUid2, testBufferUid3);

        log.info("✅ Successfully added and retrieved connection scheme: {}", testSchemeUid);
    }

    @Test
    @DisplayName("Should throw exception when adding connection scheme with duplicate UID")
    void shouldThrowExceptionWhenAddingConnectionSchemeWithDuplicateUid() {
        // Given
        createTestConnectionSchemeInDatabase();

        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(testBufferUid1, Arrays.asList(testBufferUid2));

        ConnectionSchemeBlm duplicateScheme = ConnectionSchemeBlm.builder()
                .uid(testSchemeUid) // тот же UID
                .clientUid(testClientUid)
                .schemeJson(convertTransitionsToJson(bufferTransitions))
                .usedBuffers(Arrays.asList(testBufferUid1, testBufferUid2))
                .bufferTransitions(bufferTransitions)
                .build();

        // When & Then
        assertThatThrownBy(() -> connectionSchemeRepository.add(duplicateScheme))
                .isInstanceOf(ConnectionSchemeAlreadyExistsException.class);

        log.info("✅ Correctly prevented duplicate connection scheme UID");
    }

    @Test
    @DisplayName("Should find connection scheme by UID")
    void shouldFindConnectionSchemeByUid() {
        // Given
        createTestConnectionSchemeInDatabase();

        // When
        ConnectionSchemeBlm foundScheme = connectionSchemeRepository.findByUid(testSchemeUid);

        // Then
        assertThat(foundScheme).isNotNull();
        assertThat(foundScheme.getUid()).isEqualTo(testSchemeUid);
        assertThat(foundScheme.getClientUid()).isEqualTo(testClientUid);
        assertThat(foundScheme.getUsedBuffers()).hasSize(3);

        log.info("✅ Successfully found connection scheme by UID: {}", testSchemeUid);
    }

    @Test
    @DisplayName("Should find connection schemes by client UID")
    void shouldFindConnectionSchemesByClientUid() {
        // Given
        createTestConnectionSchemeInDatabase();

        // When
        List<ConnectionSchemeBlm> foundSchemes = connectionSchemeRepository.findByClientUid(testClientUid);

        // Then
        assertThat(foundSchemes).isNotEmpty();
        assertThat(foundSchemes.get(0).getClientUid()).isEqualTo(testClientUid);
        assertThat(foundSchemes.get(0).getUid()).isEqualTo(testSchemeUid);

        log.info("✅ Successfully found connection schemes by client UID: {}", testClientUid);
    }

    @Test
    @DisplayName("Should find connection schemes by buffer UID")
    void shouldFindConnectionSchemesByBufferUid() {
        // Given
        createTestConnectionSchemeInDatabase();

        // When
        List<ConnectionSchemeBlm> foundSchemes = connectionSchemeRepository.findByBufferUid(testBufferUid1);

        // Then
        assertThat(foundSchemes).isNotEmpty();
        assertThat(foundSchemes.get(0).getUid()).isEqualTo(testSchemeUid);
        assertThat(foundSchemes.get(0).getUsedBuffers()).contains(testBufferUid1);

        log.info("✅ Successfully found connection schemes by buffer UID: {}", testBufferUid1);
    }

    @Test
    @DisplayName("Should update connection scheme successfully")
    void shouldUpdateConnectionSchemeSuccessfully() {
        // Given
        createTestConnectionSchemeInDatabase();

        // Создаем новые transitions для обновления
        Map<UUID, List<UUID>> updatedTransitions = new HashMap<>();
        updatedTransitions.put(testBufferUid1, Arrays.asList(testBufferUid3)); // Изменили переход

        ConnectionSchemeBlm updatedScheme = ConnectionSchemeBlm.builder()
                .uid(testSchemeUid)
                .clientUid(testClientUid)
                .schemeJson(convertTransitionsToJson(updatedTransitions))
                .usedBuffers(Arrays.asList(testBufferUid1, testBufferUid3)) // Обновили used buffers
                .bufferTransitions(updatedTransitions)
                .build();

        // When
        connectionSchemeRepository.update(updatedScheme);

        // Then
        ConnectionSchemeBlm foundScheme = connectionSchemeRepository.findByUid(testSchemeUid);
        assertThat(foundScheme).isNotNull();
        assertThat(foundScheme.getUsedBuffers()).hasSize(2);
        assertThat(foundScheme.getUsedBuffers()).contains(testBufferUid1, testBufferUid3);

        log.info("✅ Successfully updated connection scheme: {}", testSchemeUid);
    }

    @Test
    @DisplayName("Should delete connection scheme successfully")
    void shouldDeleteConnectionSchemeSuccessfully() {
        // Given
        createTestConnectionSchemeInDatabase();

        // Verify scheme exists
        ConnectionSchemeBlm existingScheme = connectionSchemeRepository.findByUid(testSchemeUid);
        assertThat(existingScheme).isNotNull();

        // When
        connectionSchemeRepository.delete(testSchemeUid);

        // Then - схема не должна больше существовать
        assertThatThrownBy(() -> connectionSchemeRepository.findByUid(testSchemeUid))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        log.info("✅ Successfully deleted connection scheme: {}", testSchemeUid);
    }

    @Test
    @DisplayName("Should throw exception when connection scheme not found by UID")
    void shouldThrowExceptionWhenConnectionSchemeNotFoundByUid() {
        // Given
        UUID nonExistentUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> connectionSchemeRepository.findByUid(nonExistentUid))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        log.info("✅ Correctly handled non-existent connection scheme UID");
    }

    @Test
    @DisplayName("Should check if connection scheme exists")
    void shouldCheckIfConnectionSchemeExists() {
        // Given
        createTestConnectionSchemeInDatabase();

        // When
        boolean exists = connectionSchemeRepository.exists(testSchemeUid);

        // Then
        assertThat(exists).isTrue();

        log.info("✅ Correctly checked connection scheme existence");
    }

    

    private String convertTransitionsToJson(Map<UUID, List<UUID>> bufferTransitions) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.writeValueAsString(bufferTransitions);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert transitions to JSON", e);
        }
    }
}