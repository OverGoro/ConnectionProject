package com.connection.device.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.repository.DeviceRepositorySQLImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Device Repository SQL Implementation Integration Tests")
public class DeviceRepositorySQLImplIntegrationTest extends BaseDeviceRepositoryIntegrationTest {

    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUpRepository() {
        this.deviceRepository = new DeviceRepositorySQLImpl(jdbcTemplate);
    }



    @Test
    @DisplayName("Should update device successfully")
    void shouldUpdateDeviceSuccessfully() {
        // Given
        createTestDeviceInDatabase();

        DeviceBLM updatedDevice = DeviceBLM.builder()
                .uid(testDeviceUid)
                .clientUuid(testClientUid)
                .deviceName("Updated Device Name")
                .deviceDescription("Updated Device Description")
                .build();

        // When
        deviceRepository.update(updatedDevice);

        // Then
        DeviceBLM foundDevice = deviceRepository.findByUid(testDeviceUid);

        assertThat(foundDevice).isNotNull();
        assertThat(foundDevice.getDeviceName()).isEqualTo("Updated Device Name");
        assertThat(foundDevice.getDeviceDescription()).isEqualTo("Updated Device Description");

        log.info("✅ Successfully updated device: {}", testDeviceUid);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent device")
    void shouldThrowExceptionWhenUpdatingNonExistentDevice() {
        // Given
        DeviceBLM nonExistentDevice = DeviceBLM.builder()
                .uid(UUID.randomUUID())
                .clientUuid(testClientUid)
                .deviceName("Non Existent Device")
                .deviceDescription("Non Existent Description")
                .build();

        // When & Then
        assertThatThrownBy(() -> deviceRepository.update(nonExistentDevice))
                .isInstanceOf(DeviceNotFoundException.class);

        log.info("✅ Correctly prevented update of non-existent device");
    }

    @Test
    @DisplayName("Should delete device successfully")
    void shouldDeleteDeviceSuccessfully() {
        // Given
        createTestDeviceInDatabase();

        // Verify device exists
        DeviceBLM existingDevice = deviceRepository.findByUid(testDeviceUid);
        assertThat(existingDevice).isNotNull();

        // When
        deviceRepository.delete(testDeviceUid);

        // Then - устройство не должно больше существовать
        assertThatThrownBy(() -> deviceRepository.findByUid(testDeviceUid))
                .isInstanceOf(DeviceNotFoundException.class);

        log.info("✅ Successfully deleted device: {}", testDeviceUid);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent device")
    void shouldThrowExceptionWhenDeletingNonExistentDevice() {
        // Given
        UUID nonExistentUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> deviceRepository.delete(nonExistentUid))
                .isInstanceOf(DeviceNotFoundException.class);

        log.info("✅ Correctly prevented deletion of non-existent device");
    }

    @Test
    @DisplayName("Should find devices by client UUID")
    void shouldFindDevicesByClientUuid() {
        // Given
        createTestDeviceInDatabase();

        // Create second device for same client
        UUID secondDeviceUid = UUID.randomUUID();
        DeviceBLM secondDevice = DeviceBLM.builder()
                .uid(secondDeviceUid)
                .clientUuid(testClientUid)
                .deviceName("Second Test Device")
                .deviceDescription("Second Test Description")
                .build();
        deviceRepository.add(secondDevice);

        // When
        List<DeviceBLM> devices = deviceRepository.findByClientUuid(testClientUid);

        // Then
        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(DeviceBLM::getUid)
                .contains(testDeviceUid, secondDeviceUid);

        log.info("✅ Successfully found {} devices for client: {}", devices.size(), testClientUid);
    }

    @Test
    @DisplayName("Should return empty list when no devices found for client")
    void shouldReturnEmptyListWhenNoDevicesFoundForClient() {
        // Given
        UUID clientWithNoDevices = UUID.randomUUID();

        // When
        List<DeviceBLM> devices = deviceRepository.findByClientUuid(clientWithNoDevices);

        // Then
        assertThat(devices).isEmpty();

        log.info("✅ Correctly returned empty list for client with no devices");
    }

    @Test
    @DisplayName("Should check if device exists")
    void shouldCheckIfDeviceExists() {
        // Given
        createTestDeviceInDatabase();

        // When & Then
        assertThat(deviceRepository.exists(testDeviceUid)).isTrue();

        UUID nonExistentUid = UUID.randomUUID();
        assertThat(deviceRepository.exists(nonExistentUid)).isFalse();

        log.info("✅ Correctly checked device existence");
    }

    @Test
    @DisplayName("Should check if device exists by client and name")
    void shouldCheckIfDeviceExistsByClientAndName() {
        // Given
        createTestDeviceInDatabase();

        // When & Then
        assertThat(deviceRepository.existsByClientAndName(testClientUid, testDeviceName)).isTrue();
        assertThat(deviceRepository.existsByClientAndName(testClientUid, "Non Existent Name")).isFalse();
        assertThat(deviceRepository.existsByClientAndName(UUID.randomUUID(), testDeviceName)).isFalse();

        log.info("✅ Correctly checked device existence by client and name");
    }
}