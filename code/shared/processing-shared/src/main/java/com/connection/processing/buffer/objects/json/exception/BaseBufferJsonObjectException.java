// BaseBufferJsonObjectException.java
package com.connection.processing.buffer.objects.json.exception;

public class BaseBufferJsonObjectException extends RuntimeException {
    private final String objectUid;

    public BaseBufferJsonObjectException(String objectUid) {
        super("buffer json object");
        this.objectUid = objectUid;
    }

    public String toString() {
        return super.toString() + "\nobject: " + objectUid;
    }
}