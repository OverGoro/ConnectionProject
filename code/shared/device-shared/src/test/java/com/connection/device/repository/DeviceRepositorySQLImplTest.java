package com.connection.device.repository;

import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Repository Tests - SQL implementation tests")
class DeviceRepositorySQLImplTest {

        @Mock
        private NamedParameterJdbcTemplate jdbcTemplate;

        @InjectMocks
        private DeviceRepositorySQLImpl repository;

        private DeviceDALM testDevice;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                testDevice = createValidDeviceDALM();
        }

        @Test
        @Order(1)
        @DisplayName("Add device - Positive")
        @SuppressWarnings("unchecked")
        void testAddDevice_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(
                                anyString(),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class))).thenThrow(new EmptyResultDataAccessException(1)); // Device
                                                                                                         // doesn't
                                                                                                         // exist

                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.add(testDevice);

                // Assert
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, client_uuid, device_name, device_description FROM core.device WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
                verify(jdbcTemplate, times(1)).update(
                                eq("INSERT INTO core.device (uid, client_uuid, device_name, device_description) VALUES (:uid, :client_uuid, :device_name, :device_description)"),
                                any(MapSqlParameterSource.class));
        }

        @Test
        @Order(2)
        @DisplayName("Add existing device - Negative")
        @SuppressWarnings("unchecked")
        void testAddExistingDevice_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDevice); // Device exists

                // Act & Assert
                assertThatThrownBy(() -> repository.add(testDevice))
                                .isInstanceOf(DeviceAlreadyExistsException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @Test
        @Order(3)
        @DisplayName("Update device - Positive")
        @SuppressWarnings("unchecked")
        void testUpdateDevice_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDevice); // Device exists
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.update(testDevice);

                // Assert
                verify(jdbcTemplate, times(1)).update(
                                eq("UPDATE core.device SET device_name = :device_name, device_description = :device_description WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @Test
        @Order(4)
        @DisplayName("Update non-existent device - Negative")
        @SuppressWarnings("unchecked")
        void testUpdateNonExistentDevice_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1)); // Device doesn't exist

                // Act & Assert
                assertThatThrownBy(() -> repository.update(testDevice))
                                .isInstanceOf(DeviceNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @Test
        @Order(5)
        @DisplayName("Delete device - Positive")
        @SuppressWarnings("unchecked")
        void testDeleteDevice_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDevice); // Device exists
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.delete(testDevice.getUid());

                // Assert
                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM core.device WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @Test
        @Order(6)
        @DisplayName("Delete non-existent device - Negative")
        @SuppressWarnings("unchecked")
        void testDeleteNonExistentDevice_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1)); // Device doesn't exist

                // Act & Assert
                assertThatThrownBy(() -> repository.delete(testDevice.getUid()))
                                .isInstanceOf(DeviceNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @Test
        @Order(7)
        @DisplayName("Find device by UID - Positive")
        @SuppressWarnings("unchecked")
        void testFindByUid_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDevice);

                // Act
                DeviceDALM result = repository.findByUid(testDevice.getUid());

                // Assert
                assertThat(result).isEqualTo(testDevice);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, client_uuid, device_name, device_description FROM core.device WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @Test
        @Order(8)
        @DisplayName("Find non-existent device by UID - Negative")
        @SuppressWarnings("unchecked")
        void testFindByUid_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                // Act & Assert
                assertThatThrownBy(() -> repository.findByUid(testDevice.getUid()))
                                .isInstanceOf(DeviceNotFoundException.class);
        }

        @Test
        @Order(9)
        @DisplayName("Check device exists - Positive")
        @SuppressWarnings("unchecked")
        void testExists_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDevice);

                // Act
                boolean result = repository.exists(testDevice.getUid());

                // Assert
                assertThat(result).isTrue();
        }

        @Test
        @Order(10)
        @DisplayName("Check device exists - Negative")
        @SuppressWarnings("unchecked")
        void testExists_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                // Act
                boolean result = repository.exists(testDevice.getUid());

                // Assert
                assertThat(result).isFalse();
        }
}