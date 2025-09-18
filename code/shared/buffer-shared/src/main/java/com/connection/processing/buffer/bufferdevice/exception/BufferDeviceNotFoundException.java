// BufferDeviceNotFoundException.java
package com.connection.processing.buffer.bufferdevice.exception;

public class BufferDeviceNotFoundException extends BaseBufferDeviceException {
    public BufferDeviceNotFoundException(String bufferUid, String deviceUid) {
        super(bufferUid, deviceUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer-device relationship not found";
        return res;
    }
}