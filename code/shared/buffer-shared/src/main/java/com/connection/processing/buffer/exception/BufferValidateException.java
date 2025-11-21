
package com.connection.processing.buffer.exception;

/** . */
public class BufferValidateException extends BaseBufferException {
    private final String description;

    /** . */
    public BufferValidateException(String bufferUid, String description) {
        super(bufferUid);
        this.description = description;
    }

    /** . */
    public String toString() {
        return super.toString() + "\ndescription: " + description;
    }
}
