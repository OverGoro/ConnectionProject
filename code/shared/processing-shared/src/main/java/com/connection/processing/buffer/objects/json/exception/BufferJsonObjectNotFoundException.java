// BufferJsonObjectNotFoundException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonObjectNotFoundException extends BaseBufferJsonObjectException {
    public BufferJsonObjectNotFoundException(String objectUid) {
        super(objectUid);
    }

    public String toString() {
        return super.toString() + "\ndescription: buffer json object not found";
    }
}