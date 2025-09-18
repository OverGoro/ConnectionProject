package com.connection.processing.buffer.bufferdevice.repository;

import static com.connection.processing.buffer.bufferdevice.mother.BufferDeviceObjectMother.createValidBufferDeviceDALM;
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

import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceAlreadyExistsException;
import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceNotFoundException;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("BufferDevice Repository Tests - SQL implementation tests")
class BufferDeviceRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private BufferDeviceRepositorySQLImpl repository;

    private BufferDeviceDALM testBufferDevice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testBufferDevice = createValidBufferDeviceDALM();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add buffer-device relationship - Positive")
    void testAddBufferDevice_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testBufferDevice);

        verify(jdbcTemplate, times(1)).queryForObject(
                eq("SELECT buffer_uid, device_uid FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid AND device_uid = :device_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO processing.buffer_devices (buffer_uid, device_uid) VALUES (:buffer_uid, :device_uid)"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing buffer-device relationship - Negative")
    void testAddExistingBufferDevice_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testBufferDevice);

        assertThatThrownBy(() -> repository.add(testBufferDevice))
                .isInstanceOf(BufferDeviceAlreadyExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete buffer-device relationship - Positive")
    void testDeleteBufferDevice_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testBufferDevice);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.delete(testBufferDevice);

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid AND device_uid = :device_uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete non-existent buffer-device relationship - Negative")
    void testDeleteNonExistentBufferDevice_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.delete(testBufferDevice))
                .isInstanceOf(BufferDeviceNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Delete all by buffer UID - Positive")
    void testDeleteAllByBufferUid_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.deleteAllByBufferUid(testBufferDevice.getBufferUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid"),
                any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Delete all by device UID - Positive")
    void testDeleteAllByDeviceUid_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.deleteAllByDeviceUid(testBufferDevice.getDeviceUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.buffer_devices WHERE device_uid = :device_uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add devices to buffer - Positive")
    void testAddDevicesToBuffer_Positive() {
        List<UUID> deviceUids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class))).thenReturn(new int[]{1, 1});

        repository.addDevicesToBuffer(testBufferDevice.getBufferUid(), deviceUids);

        verify(jdbcTemplate, times(2)).queryForObject(
                eq("SELECT buffer_uid, device_uid FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid AND device_uid = :device_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
        verify(jdbcTemplate, times(1)).batchUpdate(
                eq("INSERT INTO processing.buffer_devices (buffer_uid, device_uid) VALUES (:buffer_uid, :device_uid)"),
                any(MapSqlParameterSource[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing device to buffer - Negative")
    void testAddExistingDeviceToBuffer_Negative() {
        List<UUID> deviceUids = List.of(UUID.randomUUID());
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testBufferDevice);

        assertThatThrownBy(() -> repository.addDevicesToBuffer(testBufferDevice.getBufferUid(), deviceUids))
                .isInstanceOf(BufferDeviceAlreadyExistsException.class);

        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check relationship exists - Positive")
    void testExists_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testBufferDevice);

        boolean result = repository.exists(testBufferDevice.getBufferUid(), testBufferDevice.getDeviceUid());

        assertThat(result).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Check relationship exists - Negative")
    void testExists_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        boolean result = repository.exists(testBufferDevice.getBufferUid(), testBufferDevice.getDeviceUid());

        assertThat(result).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find device UIDs by buffer UID - Positive")
    void testFindDeviceUidsByBufferUid_Positive() {
        List<UUID> expectedUids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expectedUids);

        List<UUID> result = repository.findDeviceUidsByBufferUid(testBufferDevice.getBufferUid());

        assertThat(result).isEqualTo(expectedUids);
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT device_uid FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find buffer UIDs by device UID - Positive")
    void testFindBufferUidsByDeviceUid_Positive() {
        List<UUID> expectedUids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expectedUids);

        List<UUID> result = repository.findBufferUidsByDeviceUid(testBufferDevice.getDeviceUid());

        assertThat(result).isEqualTo(expectedUids);
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT buffer_uid FROM processing.buffer_devices WHERE device_uid = :device_uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }
}