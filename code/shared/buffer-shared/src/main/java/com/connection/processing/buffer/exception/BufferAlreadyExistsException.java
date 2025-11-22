
package com.connection.processing.buffer.exception;

/** . */
public class BufferAlreadyExistsException extends BaseBufferException {
    /** . */
    public BufferAlreadyExistsException(String bufferUid) {
        super(bufferUid);
    }

    /** . */
    public String toString() {
        return super.toString() + "\ndescription: buffer already exists";
    }
}
