// BaseBufferDeviceException.java
package com.connection.processing.buffer.bufferdevice.exception;

public class BaseBufferDeviceException extends RuntimeException {
    private final String bufferUid;
    private final String deviceUid;

    public BaseBufferDeviceException(String bufferUid, String deviceUid) {
        super("buffer_device");
        this.bufferUid = bufferUid;
        this.deviceUid = deviceUid;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "buffer: " + bufferUid;
        res += "\n" + "device: " + deviceUid;
        return res;
    }
}