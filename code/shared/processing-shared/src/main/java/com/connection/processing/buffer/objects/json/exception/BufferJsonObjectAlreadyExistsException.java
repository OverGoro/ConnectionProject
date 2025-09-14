// BufferJsonObjectAlreadyExistsException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonObjectAlreadyExistsException extends BaseBufferJsonObjectException {
    public BufferJsonObjectAlreadyExistsException(String objectUid) {
        super(objectUid);
    }

    public String toString() {
        return super.toString() + "\ndescription: buffer json object already exists";
    }
}