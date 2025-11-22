package com.connection.scheme.repository;

import static com.connection.scheme.mother.ConnectionSchemeObjectMother.createConnectionSchemeDalmWithEmptyUsedBuffers;
import static com.connection.scheme.mother.ConnectionSchemeObjectMother.createValidConnectionSchemeBlm;
import static com.connection.scheme.mother.ConnectionSchemeObjectMother.createValidConnectionSchemeDalm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
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

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Repository Tests - Sql implementation tests")
class ConnectionSchemeRepositorySqlImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private ConnectionSchemeRepositorySqlImpl repository;

    private ConnectionSchemeDalm testSchemeDalm;
    private ConnectionSchemeBlm testSchemeBlm;
    private ConnectionSchemeConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testSchemeDalm = createValidConnectionSchemeDalm();
        testSchemeBlm = createValidConnectionSchemeBlm();
        converter = new ConnectionSchemeConverter();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add scheme - Positive")
    void testAddScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        repository.add(testSchemeBlm);

        verify(jdbcTemplate, times(1)).queryForObject(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.uid = :uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(eq(
                "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) VALUES (:uid, :client_uid, :scheme_json::jsonb)"),
                any(MapSqlParameterSource.class));

        // ИСПРАВЛЕНО: Проверяем сохранение связей с буферами в таблицу connection_scheme_buffer
        verify(jdbcTemplate, times(testSchemeDalm.getUsedBuffers().size()))
                .update(eq(
                        "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:uid, :scheme_uid, :buffer_uid)"),
                        any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add scheme with empty used buffers - Positive")
    void testAddSchemeWithEmptyUsedBuffers_Positive() {
        ConnectionSchemeBlm scheme = converter
                .toBlm(createConnectionSchemeDalmWithEmptyUsedBuffers());
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        repository.add(scheme);

        verify(jdbcTemplate, times(1)).update(eq(
                "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) VALUES (:uid, :client_uid, :scheme_json::jsonb)"),
                any(MapSqlParameterSource.class));

        // ИСПРАВЛЕНО: Не должно быть вставок в таблицу connection_scheme_buffer для пустых used buffers
        verify(jdbcTemplate, never()).update(eq(
                "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing scheme - Negative")
    void testAddExistingScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm);

        assertThatThrownBy(() -> repository.add(testSchemeBlm))
                .isInstanceOf(ConnectionSchemeAlreadyExistsException.class);

        verify(jdbcTemplate, never()).update(eq(
                "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) VALUES (:uid, :client_uid, :scheme_json::jsonb)"),
                any(MapSqlParameterSource.class));
        verify(jdbcTemplate, never()).update(eq(
                "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update scheme - Positive")
    void testUpdateScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        repository.update(testSchemeBlm);

        // ДОБАВИТЬ: Проверка SELECT запроса для проверки существования схемы
        verify(jdbcTemplate, times(1)).queryForObject(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.uid = :uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));

        verify(jdbcTemplate, times(1)).update(eq(
                "UPDATE processing.connection_scheme SET scheme_json = :scheme_json::jsonb WHERE uid = :uid"),
                any(MapSqlParameterSource.class));

        verify(jdbcTemplate, times(1)).update(eq(
                "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class));
        verify(jdbcTemplate, times(testSchemeDalm.getUsedBuffers().size()))
                .update(eq(
                        "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:uid, :scheme_uid, :buffer_uid)"),
                        any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update scheme with empty used buffers - Positive")
    void testUpdateSchemeWithEmptyUsedBuffers_Positive() {
        ConnectionSchemeBlm scheme = converter
                .toBlm(createConnectionSchemeDalmWithEmptyUsedBuffers());
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(converter.toDalm(scheme));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        repository.update(scheme);

        verify(jdbcTemplate, times(1)).update(eq(
                "UPDATE processing.connection_scheme SET scheme_json = :scheme_json::jsonb WHERE uid = :uid"),
                any(MapSqlParameterSource.class));

        // ИСПРАВЛЕНО: Должны удалить старые связи, но не вставлять новые
        verify(jdbcTemplate, times(1)).update(eq(
                "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class));
        verify(jdbcTemplate, never()).update(eq(
                "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) VALUES (:uid, :scheme_uid, :buffer_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Update non-existent scheme - Negative")
    void testUpdateNonExistentScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.update(testSchemeBlm))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete scheme - Positive")
    void testDeleteScheme_Positive() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        repository.delete(testSchemeDalm.getUid());

        // ИСПРАВЛЕНО: Связи удаляются каскадно, поэтому проверяем только удаление схемы
        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.connection_scheme WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete non-existent scheme - Negative")
    void testDeleteNonExistentScheme_Negative() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.delete(testSchemeDalm.getUid()))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find scheme by UID - Positive")
    void testFindByUid_Positive() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm);

        // ИСПРАВЛЕНО: Мокаем запрос для получения used buffers из connection_scheme_buffer
        when(jdbcTemplate.query(eq(
                "SELECT csb.buffer_uid FROM processing.connection_scheme_buffer csb WHERE csb.scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm.getUsedBuffers());

        ConnectionSchemeBlm result =
                repository.findByUid(testSchemeDalm.getUid());

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(testSchemeBlm.getUid());
        assertThat(result.getClientUid())
                .isEqualTo(testSchemeBlm.getClientUid());
        assertThat(result.getSchemeJson())
                .isEqualTo(testSchemeBlm.getSchemeJson());
        assertThat(result.getUsedBuffers())
                .isEqualTo(testSchemeBlm.getUsedBuffers());

        verify(jdbcTemplate, times(1)).queryForObject(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.uid = :uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find non-existent scheme by UID - Negative")
    void testFindByUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(testSchemeDalm.getUid()))
                .isInstanceOf(ConnectionSchemeNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find schemes by client UID - Positive")
    void testFindByClientUid_Positive() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(RowMapper.class))).thenReturn(List.of(testSchemeDalm));

        // ИСПРАВЛЕНО: Мокаем запрос для получения used buffers для каждой схемы из connection_scheme_buffer
        when(jdbcTemplate.query(eq(
                "SELECT csb.buffer_uid FROM processing.connection_scheme_buffer csb WHERE csb.scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm.getUsedBuffers());

        List<ConnectionSchemeBlm> result =
                repository.findByClientUid(testSchemeDalm.getClientUid());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUid()).isEqualTo(testSchemeDalm.getUid());
        assertThat(result.get(0).getClientUid())
                .isEqualTo(testSchemeDalm.getClientUid());
        assertThat(result.get(0).getSchemeJson())
                .isEqualTo(testSchemeDalm.getSchemeJson());
        assertThat(result.get(0).getUsedBuffers())
                .isEqualTo(testSchemeDalm.getUsedBuffers());

        verify(jdbcTemplate, times(1)).query(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.client_uid = :client_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find schemes by client UID with empty result - Positive")
    void testFindByClientUid_EmptyResult() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(RowMapper.class))).thenReturn(List.of());

        List<ConnectionSchemeBlm> result =
                repository.findByClientUid(testSchemeDalm.getClientUid());

        assertThat(result).isEmpty();
        verify(jdbcTemplate, times(1)).query(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs WHERE cs.client_uid = :client_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find schemes by buffer UID - Positive")
    void testFindByBufferUid_Positive() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(RowMapper.class))).thenReturn(List.of(testSchemeDalm));

        // ИСПРАВЛЕНО: Мокаем запрос для получения used buffers для каждой схемы
        when(jdbcTemplate.query(eq(
                "SELECT csb.buffer_uid FROM processing.connection_scheme_buffer csb WHERE csb.scheme_uid = :scheme_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm.getUsedBuffers());

        List<ConnectionSchemeBlm> result =
                repository.findByBufferUid(UUID.randomUUID());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUid()).isEqualTo(testSchemeDalm.getUid());
        assertThat(result.get(0).getClientUid())
                .isEqualTo(testSchemeDalm.getClientUid());

        verify(jdbcTemplate, times(1)).query(eq(
                "SELECT cs.uid, cs.client_uid, cs.scheme_json FROM processing.connection_scheme cs INNER JOIN processing.connection_scheme_buffer csb ON cs.uid = csb.scheme_uid WHERE csb.buffer_uid = :buffer_uid"),
                any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check scheme exists - Positive")
    void testExists_Positive() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenReturn(testSchemeDalm);

        boolean result = repository.exists(testSchemeDalm.getUid());

        assertThat(result).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check scheme exists - Negative")
    void testExists_Negative() {
        when(jdbcTemplate.queryForObject(anyString(),
                any(MapSqlParameterSource.class), any(RowMapper.class)))
                        .thenThrow(new EmptyResultDataAccessException(1));

        boolean result = repository.exists(testSchemeDalm.getUid());

        assertThat(result).isFalse();
    }
}
