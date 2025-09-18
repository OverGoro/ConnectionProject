package com.connection.processing.buffer.objects.json.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Json Data Exception Tests")
class BufferJsonDataExceptionTest {

    @Test
    @DisplayName("BaseBufferJsonDataException toString format")
    void testBaseBufferJsonDataExceptionToString() {
        String dataUid = "test-uid-123";
        BaseBufferJsonDataException exception = new BaseBufferJsonDataException(dataUid);
        String result = exception.toString();
        assertThat(result).contains(dataUid);
    }

    @Test
    @DisplayName("BufferJsonDataValidateException toString format")
    void testBufferJsonDataValidateExceptionToString() {
        String dataUid = "test-uid-456";
        String description = "Validation failed";
        BufferJsonDataValidateException exception = new BufferJsonDataValidateException(dataUid, description);
        String result = exception.toString();
        assertThat(result).contains(dataUid);
    }

    @Test
    @DisplayName("BufferJsonDataAlreadyExistsException toString format")
    void testBufferJsonDataAlreadyExistsExceptionToString() {
        String dataUid = "test-uid-789";
        BufferJsonDataAlreadyExistsException exception = new BufferJsonDataAlreadyExistsException(dataUid);
        String result = exception.toString();
        assertThat(result).contains(dataUid);
    }

    @Test
    @DisplayName("BufferJsonDataNotFoundException toString format")
    void testBufferJsonDataNotFoundExceptionToString() {
        String dataUid = "test-uid-012";
        BufferJsonDataNotFoundException exception = new BufferJsonDataNotFoundException(dataUid);
        String result = exception.toString();
        assertThat(result).contains(dataUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        BufferJsonDataValidateException validateException = new BufferJsonDataValidateException("uid", "desc");
        BufferJsonDataAlreadyExistsException existsException = new BufferJsonDataAlreadyExistsException("uid");
        BufferJsonDataNotFoundException notFoundException = new BufferJsonDataNotFoundException("uid");

        assertThat(validateException).isInstanceOf(BaseBufferJsonDataException.class);
        assertThat(existsException).isInstanceOf(BaseBufferJsonDataException.class);
        assertThat(notFoundException).isInstanceOf(BaseBufferJsonDataException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}