// BufferJsonObjectValidateException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonObjectValidateException extends BaseBufferJsonObjectException {
    private final String description;

    public BufferJsonObjectValidateException(String objectUid, String description) {
        super(objectUid);
        this.description = description;
    }

    public String toString() {
        return super.toString() + "\ndescription: " + description;
    }
}