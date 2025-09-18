package com.connection.device.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Exception Tests")
class DeviceExceptionTest {

    @Test
    @Order(1)
    @DisplayName("BaseDeviceException toString format")
    void testBaseDeviceExceptionToString() {
        // Arrange
        String deviceUid = "test-uid-123";
        BaseDeviceException exception = new BaseDeviceException(deviceUid);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains(deviceUid);
    }

    @Test
    @Order(2)
    @DisplayName("DeviceValidateException toString format")
    void testDeviceValidateExceptionToString() {
        // Arrange
        String deviceUid = "test-uid-456";
        String description = "Validation failed";
        DeviceValidateException exception = new DeviceValidateException(deviceUid, description);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains(deviceUid);
    }

    @Test
    @Order(3)
    @DisplayName("DeviceAlreadyExistsException toString format")
    void testDeviceAlreadyExistsExceptionToString() {
        // Arrange
        String deviceUid = "test-uid-789";
        DeviceAlreadyExistsException exception = new DeviceAlreadyExistsException(deviceUid);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains(deviceUid);
    }

    @Test
    @Order(4)
    @DisplayName("DeviceNotFoundException toString format")
    void testDeviceNotFoundExceptionToString() {
        // Arrange
        String deviceUid = "test-uid-012";
        DeviceNotFoundException exception = new DeviceNotFoundException(deviceUid);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains(deviceUid);
    }

    @Test
    @Order(5)
    @DisplayName("DeviceAddException toString format")
    void testDeviceAddExceptionToString() {
        // Arrange
        String deviceUid = "test-uid-345";
        DeviceAddException exception = new DeviceAddException(deviceUid);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains(deviceUid);
    }

    @Test
    @Order(6)
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        // Arrange & Act
        DeviceValidateException validateException = new DeviceValidateException("uid", "desc");
        DeviceAlreadyExistsException existsException = new DeviceAlreadyExistsException("uid");
        DeviceNotFoundException notFoundException = new DeviceNotFoundException("uid");
        DeviceAddException addException = new DeviceAddException("uid");

        // Assert
        assertThat(validateException).isInstanceOf(BaseDeviceException.class);
        assertThat(existsException).isInstanceOf(BaseDeviceException.class);
        assertThat(notFoundException).isInstanceOf(BaseDeviceException.class);
        assertThat(addException).isInstanceOf(BaseDeviceException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}