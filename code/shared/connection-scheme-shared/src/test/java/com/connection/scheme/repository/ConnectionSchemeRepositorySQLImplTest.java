// ConnectionSchemeRepositorySQLImplTest.java
package com.connection.scheme.repository;

import static com.connection.scheme.mother.ConnectionSchemeObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Repository Tests - SQL implementation tests")
class ConnectionSchemeRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private ConnectionSchemeRepositorySQLImpl repository;

    private ConnectionSchemeDALM testScheme;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testScheme = createValidConnectionSchemeDALM();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add scheme - Positive")
    void testAddScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testScheme);

        verify(jdbcTemplate, times(1)).queryForObject(
                eq("SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.uid = :uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) VALUES (:uid, :client_uid, :scheme_json)"),
                any(MapSqlParameterSource.class));
        
        // Verify that used buffers are saved
        verify(jdbcTemplate, times(testScheme.getUsedBuffers().size())).update(
                eq("INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:buffer_relation_uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add scheme with empty used buffers - Positive")
    void testAddSchemeWithEmptyUsedBuffers_Positive() {
        ConnectionSchemeDALM scheme = createConnectionSchemeDALMWithUsedBuffers(Arrays.asList());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(scheme);

        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) VALUES (:uid, :client_uid, :scheme_json)"),
                any(MapSqlParameterSource.class));
        
        // No buffer relations should be inserted for empty used buffers
        verify(jdbcTemplate, never()).update(
                eq("INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:buffer_relation_uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing scheme - Negative")
    void testAddExistingScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testScheme);

        assertThatThrownBy(() -> repository.add(testScheme))
                .isInstanceOf(ConnectionSchemeAlreadyExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update scheme - Positive")
    void testUpdateScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testScheme);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.update(testScheme);

        verify(jdbcTemplate, times(1)).update(
                eq("UPDATE processing.connection_scheme SET scheme_json = :scheme_json WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
        
        // Verify that buffer relations are updated
        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class));
        verify(jdbcTemplate, times(testScheme.getUsedBuffers().size())).update(
                eq("INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:buffer_relation_uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update non-existent scheme - Negative")
    void testUpdateNonExistentScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.update(testScheme))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete scheme - Positive")
    void testDeleteScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testScheme);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.delete(testScheme.getUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.connection_scheme WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete non-existent scheme - Negative")
    void testDeleteNonExistentScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.delete(testScheme.getUid()))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find scheme by UID - Positive")
    void testFindByUid_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testScheme);

        ConnectionSchemeDALM result = repository.findByUid(testScheme.getUid());

        assertThat(result).isEqualTo(testScheme);
        assertThat(result.getUsedBuffers()).isEqualTo(testScheme.getUsedBuffers());
        verify(jdbcTemplate, times(1)).queryForObject(
                eq("SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.uid = :uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find non-existent scheme by UID - Negative")
    void testFindByUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(testScheme.getUid()))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find schemes by client UID - Positive")
    void testFindByClientUid_Positive() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(testScheme));

        List<ConnectionSchemeDALM> result = repository.findByClientUid(testScheme.getClientUid());

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testScheme);
        assertThat(result.get(0).getUsedBuffers()).isEqualTo(testScheme.getUsedBuffers());
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.client_uid = :client_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find schemes by buffer UID - Positive")
    void testFindByBufferUid_Positive() {
        UUID bufferUid = UUID.randomUUID();
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(testScheme));

        List<ConnectionSchemeDALM> result = repository.findByBufferUid(bufferUid);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testScheme);
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs JOIN processing.connection_scheme_buffer csb ON cs.uid = csb.scheme_uid WHERE csb.buffer_uid = :buffer_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check scheme exists - Positive")
    void testExists_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testScheme);

        boolean result = repository.exists(testScheme.getUid());

        assertThat(result).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check scheme exists - Negative")
    void testExists_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        boolean result = repository.exists(testScheme.getUid());

        assertThat(result).isFalse();
    }
}