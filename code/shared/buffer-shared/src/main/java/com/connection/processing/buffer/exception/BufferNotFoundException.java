
package com.connection.processing.buffer.exception;

/** . */
public class BufferNotFoundException extends BaseBufferException {
    /** . */
    public BufferNotFoundException(String bufferUid) {
        super(bufferUid);
    }

    /** . */
    public String toString() {
        return super.toString() + "\ndescription: buffer not found";
    }
}
