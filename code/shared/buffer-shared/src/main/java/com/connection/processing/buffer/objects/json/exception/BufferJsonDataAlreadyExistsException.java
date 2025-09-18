// BufferJsonDataAlreadyExistsException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonDataAlreadyExistsException extends BaseBufferJsonDataException {
    public BufferJsonDataAlreadyExistsException(String dataUid) {
        super(dataUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer JSON data already exists";
        return res;
    }
}