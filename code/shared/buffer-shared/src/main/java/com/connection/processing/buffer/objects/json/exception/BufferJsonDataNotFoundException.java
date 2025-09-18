// BufferJsonDataNotFoundException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonDataNotFoundException extends BaseBufferJsonDataException {
    public BufferJsonDataNotFoundException(String dataUid) {
        super(dataUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer JSON data not found";
        return res;
    }
}