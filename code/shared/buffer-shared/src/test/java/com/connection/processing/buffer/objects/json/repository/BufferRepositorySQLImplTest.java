package com.connection.processing.buffer.objects.json.repository;

import static com.connection.processing.buffer.objects.json.mother.BufferJsonDataObjectMother.createValidBufferJsonDataDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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

import com.connection.processing.buffer.objects.json.exception.BufferJsonDataAlreadyExistsException;
import com.connection.processing.buffer.objects.json.exception.BufferJsonDataNotFoundException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Json Data Repository Tests - SQL implementation tests")
class BufferJsonDataRepositorySQLImplTest {

        @Mock
        private NamedParameterJdbcTemplate jdbcTemplate;

        @InjectMocks
        private BufferJsonDataRepositorySQLImpl repository;

        private BufferJsonDataDALM testData;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                testData = createValidBufferJsonDataDALM();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add data - Positive")
        void testAddData_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.add(testData);

                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
                verify(jdbcTemplate, times(1)).update(
                                eq("INSERT INTO processing.buffer_json_datas (uid, buffer_uid, data, created_at) VALUES (:uid, :buffer_uid, :data, :created_at)"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add existing data - Negative")
        void testAddExistingData_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);

                assertThatThrownBy(() -> repository.add(testData))
                                .isInstanceOf(BufferJsonDataAlreadyExistsException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete data - Positive")
        void testDeleteData_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.delete(testData.getUid());

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM processing.buffer_json_datas WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete non-existent data - Negative")
        void testDeleteNonExistentData_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.delete(testData.getUid()))
                                .isInstanceOf(BufferJsonDataNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @Test
        @DisplayName("Delete data by buffer UID - Positive")
        void testDeleteByBufferUid_Positive() {
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.deleteByBufferUid(testData.getBufferUid());

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid"),
                                any(MapSqlParameterSource.class));
        }

        @Test
        @DisplayName("Delete old data - Positive")
        void testDeleteOldData_Positive() {
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                Instant olderThan = Instant.now().minusSeconds(3600);
                repository.deleteOldData(olderThan);

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM processing.buffer_json_datas WHERE created_at < :older_than"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find data by UID - Positive")
        void testFindByUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);

                BufferJsonDataDALM result = repository.findByUid(testData.getUid());

                assertThat(result).isEqualTo(testData);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find non-existent data by UID - Negative")
        void testFindByUid_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.findByUid(testData.getUid()))
                                .isInstanceOf(BufferJsonDataNotFoundException.class);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find data by buffer UID - Positive")
        void testFindByBufferUid_Positive() {
                when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(List.of(testData));

                List<BufferJsonDataDALM> result = repository.findByBufferUid(testData.getBufferUid());

                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(testData);
                verify(jdbcTemplate, times(1)).query(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find newest data by buffer UID - Positive")
        void testFindNewestByBufferUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);

                BufferJsonDataDALM result = repository.findNewestByBufferUid(testData.getBufferUid());

                assertThat(result).isEqualTo(testData);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC LIMIT 1"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find oldest data by buffer UID - Positive")
        void testFindOldestByBufferUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);

                BufferJsonDataDALM result = repository.findOldestByBufferUid(testData.getBufferUid());

                assertThat(result).isEqualTo(testData);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid ORDER BY created_at ASC LIMIT 1"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find newest N data by buffer UID - Positive")
        void testFindNewestByBufferUidWithLimit_Positive() {
                when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(List.of(testData));

                List<BufferJsonDataDALM> result = repository.findNewestByBufferUid(testData.getBufferUid(), 5);

                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(testData);
                verify(jdbcTemplate, times(1)).query(
                                eq("SELECT uid, buffer_uid, data, created_at FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC LIMIT :limit"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Check data exists - Positive")
        void testExists_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testData);

                boolean result = repository.exists(testData.getUid());

                assertThat(result).isTrue();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Check data exists - Negative")
        void testExists_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                boolean result = repository.exists(testData.getUid());

                assertThat(result).isFalse();
        }
        
        @Test
        @DisplayName("Count data by buffer UID - Positive")
        void testCountByBufferUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                                .thenReturn(5);

                int result = repository.countByBufferUid(testData.getBufferUid());

                assertThat(result).isEqualTo(5);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT COUNT(*) FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid"),
                                any(MapSqlParameterSource.class),
                                eq(Integer.class));
        }
}