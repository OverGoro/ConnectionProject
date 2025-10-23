package com.connection.device.repository;

import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBLM;
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

import java.util.List;

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

import com.connection.device.converter.DeviceConverter;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Repository Tests - SQL implementation tests")
class DeviceRepositorySQLImplTest {

        @Mock
        private NamedParameterJdbcTemplate jdbcTemplate;

        @InjectMocks
        private DeviceRepositorySQLImpl repository;

        private DeviceDALM testDeviceDALM;
        private DeviceBLM testDeviceBLM;
        private DeviceConverter converter;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                testDeviceDALM = createValidDeviceDALM();
                testDeviceBLM = createValidDeviceBLM();
                converter = new DeviceConverter();
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
                                any(RowMapper.class))).thenThrow(new EmptyResultDataAccessException(1)); // Device doesn't exist

                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.add(testDeviceBLM);

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
                                .thenReturn(testDeviceDALM); // Device exists

                // Act & Assert
                assertThatThrownBy(() -> repository.add(testDeviceBLM))
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
                                .thenReturn(testDeviceDALM); // Device exists
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.update(testDeviceBLM);

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
                assertThatThrownBy(() -> repository.update(testDeviceBLM))
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
                                .thenReturn(testDeviceDALM); // Device exists
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                // Act
                repository.delete(testDeviceDALM.getUid());

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
                assertThatThrownBy(() -> repository.delete(testDeviceDALM.getUid()))
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
                                .thenReturn(testDeviceDALM);

                // Act
                DeviceBLM result = repository.findByUid(testDeviceDALM.getUid());

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getUid()).isEqualTo(testDeviceBLM.getUid());
                assertThat(result.getClientUuid()).isEqualTo(testDeviceBLM.getClientUuid());
                assertThat(result.getDeviceName()).isEqualTo(testDeviceBLM.getDeviceName());
                assertThat(result.getDeviceDescription()).isEqualTo(testDeviceBLM.getDeviceDescription());
                
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
                assertThatThrownBy(() -> repository.findByUid(testDeviceDALM.getUid()))
                                .isInstanceOf(DeviceNotFoundException.class);
        }

        @Test
        @Order(9)
        @DisplayName("Find devices by client UUID - Positive")
        @SuppressWarnings("unchecked")
        void testFindByClientUuid_Positive() {
                // Arrange
                when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(List.of(testDeviceDALM));

                // Act
                List<DeviceBLM> result = repository.findByClientUuid(testDeviceDALM.getClientUuid());

                // Assert
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getUid()).isEqualTo(testDeviceBLM.getUid());
                assertThat(result.get(0).getClientUuid()).isEqualTo(testDeviceBLM.getClientUuid());
                assertThat(result.get(0).getDeviceName()).isEqualTo(testDeviceBLM.getDeviceName());
                
                verify(jdbcTemplate, times(1)).query(
                                eq("SELECT uid, client_uuid, device_name, device_description FROM core.device WHERE client_uuid = :client_uuid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @Test
        @Order(10)
        @DisplayName("Check device exists - Positive")
        @SuppressWarnings("unchecked")
        void testExists_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDeviceDALM);

                // Act
                boolean result = repository.exists(testDeviceDALM.getUid());

                // Assert
                assertThat(result).isTrue();
        }

        @Test
        @Order(11)
        @DisplayName("Check device exists - Negative")
        @SuppressWarnings("unchecked")
        void testExists_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                // Act
                boolean result = repository.exists(testDeviceDALM.getUid());

                // Assert
                assertThat(result).isFalse();
        }

        @Test
        @Order(12)
        @DisplayName("Check device exists by client and name - Positive")
        @SuppressWarnings("unchecked")
        void testExistsByClientAndName_Positive() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testDeviceDALM);

                // Act
                boolean result = repository.existsByClientAndName(testDeviceDALM.getClientUuid(), testDeviceDALM.getDeviceName());

                // Assert
                assertThat(result).isTrue();
                
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, client_uuid, device_name, device_description FROM core.device WHERE client_uuid = :client_uuid AND device_name = :device_name"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @Test
        @Order(13)
        @DisplayName("Check device exists by client and name - Negative")
        @SuppressWarnings("unchecked")
        void testExistsByClientAndName_Negative() {
                // Arrange
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                // Act
                boolean result = repository.existsByClientAndName(testDeviceDALM.getClientUuid(), testDeviceDALM.getDeviceName());

                // Assert
                assertThat(result).isFalse();
        }
}