// BufferDeviceAlreadyExistsException.java
package com.connection.processing.buffer.bufferdevice.exception;

public class BufferDeviceAlreadyExistsException extends BaseBufferDeviceException {
    public BufferDeviceAlreadyExistsException(String bufferUid, String deviceUid) {
        super(bufferUid, deviceUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer-device relationship already exists";
        return res;
    }
}