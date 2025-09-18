package com.connection.processing.buffer.bufferdevice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("BufferDevice Exception Tests")
class BufferDeviceExceptionTest {

    @Test
    @DisplayName("BaseBufferDeviceException toString format")
    void testBaseBufferDeviceExceptionToString() {
        String bufferUid = "test-buffer-123";
        String deviceUid = "test-device-456";
        BaseBufferDeviceException exception = new BaseBufferDeviceException(bufferUid, deviceUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
        assertThat(result).contains(deviceUid);
    }

    @Test
    @DisplayName("BufferDeviceValidateException toString format")
    void testBufferDeviceValidateExceptionToString() {
        String bufferUid = "test-buffer-789";
        String deviceUid = "test-device-012";
        String description = "Validation failed";
        BufferDeviceValidateException exception = new BufferDeviceValidateException(bufferUid, deviceUid, description);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
        assertThat(result).contains(deviceUid);
    }

    @Test
    @DisplayName("BufferDeviceAlreadyExistsException toString format")
    void testBufferDeviceAlreadyExistsExceptionToString() {
        String bufferUid = "test-buffer-345";
        String deviceUid = "test-device-678";
        BufferDeviceAlreadyExistsException exception = new BufferDeviceAlreadyExistsException(bufferUid, deviceUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
        assertThat(result).contains(deviceUid);
    }

    @Test
    @DisplayName("BufferDeviceNotFoundException toString format")
    void testBufferDeviceNotFoundExceptionToString() {
        String bufferUid = "test-buffer-901";
        String deviceUid = "test-device-234";
        BufferDeviceNotFoundException exception = new BufferDeviceNotFoundException(bufferUid, deviceUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
        assertThat(result).contains(deviceUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        BufferDeviceValidateException validateException = new BufferDeviceValidateException("buf", "dev", "desc");
        BufferDeviceAlreadyExistsException existsException = new BufferDeviceAlreadyExistsException("buf", "dev");
        BufferDeviceNotFoundException notFoundException = new BufferDeviceNotFoundException("buf", "dev");

        assertThat(validateException).isInstanceOf(BaseBufferDeviceException.class);
        assertThat(existsException).isInstanceOf(BaseBufferDeviceException.class);
        assertThat(notFoundException).isInstanceOf(BaseBufferDeviceException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}