package com.connection.processing.buffer.repository;

import static com.connection.processing.buffer.mother.BufferObjectMother.createValidBufferDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Repository Tests - SQL implementation tests")
class BufferRepositorySQLImplTest {

        @Mock
        private NamedParameterJdbcTemplate jdbcTemplate;

        @InjectMocks
        private BufferRepositorySQLImpl repository;

        private BufferDALM testBuffer;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                testBuffer = createValidBufferDALM();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add buffer - Positive")
        void testAddBuffer_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.add(testBuffer);

                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype FROM processing.buffer WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
                verify(jdbcTemplate, times(1)).update(
                                eq("INSERT INTO processing.buffer (uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype) VALUES (:uid, :connection_scheme_uid, :max_messages_number, :max_message_size, :message_prototype)"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add existing buffer - Negative")
        void testAddExistingBuffer_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testBuffer);

                assertThatThrownBy(() -> repository.add(testBuffer))
                                .isInstanceOf(BufferAlreadyExistsException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Update buffer - Positive")
        void testUpdateBuffer_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testBuffer);
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.update(testBuffer);

                verify(jdbcTemplate, times(1)).update(
                                eq("UPDATE processing.buffer SET max_messages_number = :max_messages_number, max_message_size = :max_message_size, message_prototype = :message_prototype WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Update non-existent buffer - Negative")
        void testUpdateNonExistentBuffer_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.update(testBuffer))
                                .isInstanceOf(BufferNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete buffer - Positive")
        void testDeleteBuffer_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testBuffer);
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.delete(testBuffer.getUid());

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM processing.buffer WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete non-existent buffer - Negative")
        void testDeleteNonExistentBuffer_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.delete(testBuffer.getUid()))
                                .isInstanceOf(BufferNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @Test
        @DisplayName("Delete buffers by scheme UID - Positive")
        void testDeleteByConnectionSchemeUid_Positive() {
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.deleteByConnectionSchemeUid(testBuffer.getConnectionSchemeUid());

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM processing.buffer WHERE connection_scheme_uid = :connection_scheme_uid"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find buffer by UID - Positive")
        void testFindByUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testBuffer);

                BufferDALM result = repository.findByUid(testBuffer.getUid());

                assertThat(result).isEqualTo(testBuffer);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype FROM processing.buffer WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find non-existent buffer by UID - Negative")
        void testFindByUid_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.findByUid(testBuffer.getUid()))
                                .isInstanceOf(BufferNotFoundException.class);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find buffers by scheme UID - Positive")
        void testFindByConnectionSchemeUid_Positive() {
                when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(List.of(testBuffer));

                List<BufferDALM> result = repository.findByConnectionSchemeUid(testBuffer.getConnectionSchemeUid());

                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(testBuffer);
                verify(jdbcTemplate, times(1)).query(
                                eq("SELECT uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype FROM processing.buffer WHERE connection_scheme_uid = :connection_scheme_uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Check buffer exists - Positive")
        void testExists_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testBuffer);

                boolean result = repository.exists(testBuffer.getUid());

                assertThat(result).isTrue();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Check buffer exists - Negative")
        void testExists_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                boolean result = repository.exists(testBuffer.getUid());

                assertThat(result).isFalse();
        }
}