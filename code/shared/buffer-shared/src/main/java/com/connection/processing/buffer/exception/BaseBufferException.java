
package com.connection.processing.buffer.exception;

/** . */
public class BaseBufferException extends RuntimeException {
    private final String bufferUid;

    /** . */
    public BaseBufferException(String bufferUid) {
        super("buffer");
        this.bufferUid = bufferUid;
    }

    /** . */
    public String toString() {
        return super.toString() + "\nbuffer: " + bufferUid;
    }
}
