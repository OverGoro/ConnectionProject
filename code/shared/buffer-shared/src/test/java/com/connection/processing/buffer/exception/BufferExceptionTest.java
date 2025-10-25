package com.connection.processing.buffer.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Buffer Exception Tests")
class BufferExceptionTest {

    @Test
    @DisplayName("BaseBufferException toString format")
    void testBaseBufferExceptionToString() {
        String bufferUid = "test-uid-123";
        BaseBufferException exception = new BaseBufferException(bufferUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
    }

    @Test
    @DisplayName("BufferValidateException toString format")
    void testBufferValidateExceptionToString() {
        String bufferUid = "test-uid-456";
        String description = "Validation failed";
        BufferValidateException exception = new BufferValidateException(bufferUid, description);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
    }

    @Test
    @DisplayName("BufferAlreadyExistsException toString format")
    void testBufferAlreadyExistsExceptionToString() {
        String bufferUid = "test-uid-789";
        BufferAlreadyExistsException exception = new BufferAlreadyExistsException(bufferUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
    }

    @Test
    @DisplayName("BufferNotFoundException toString format")
    void testBufferNotFoundExceptionToString() {
        String bufferUid = "test-uid-012";
        BufferNotFoundException exception = new BufferNotFoundException(bufferUid);
        String result = exception.toString();
        assertThat(result).contains(bufferUid);
    }

    @Test
    @DisplayName("Exception inheritance hierarchy")
    void testExceptionInheritance() {
        BufferValidateException validateException = new BufferValidateException("uid", "desc");
        BufferAlreadyExistsException existsException = new BufferAlreadyExistsException("uid");
        BufferNotFoundException notFoundException = new BufferNotFoundException("uid");

        assertThat(validateException).isInstanceOf(BaseBufferException.class);
        assertThat(existsException).isInstanceOf(BaseBufferException.class);
        assertThat(notFoundException).isInstanceOf(BaseBufferException.class);
        assertThat(validateException).isInstanceOf(RuntimeException.class);
    }
}