// BaseBufferJsonDataException.java
package com.connection.processing.buffer.objects.json.exception;

public class BaseBufferJsonDataException extends RuntimeException {
    private final String dataUid;

    public BaseBufferJsonDataException(String dataUid) {
        super("buffer_json_data");
        this.dataUid = dataUid;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "data: " + dataUid;
        return res;
    }
}