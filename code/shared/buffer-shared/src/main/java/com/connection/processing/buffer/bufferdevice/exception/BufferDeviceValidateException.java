// BufferDeviceValidateException.java
package com.connection.processing.buffer.bufferdevice.exception;

public class BufferDeviceValidateException extends BaseBufferDeviceException {
    String descriptionString;

    public BufferDeviceValidateException(String bufferUid, String deviceUid, String description) {
        super(bufferUid, deviceUid);
        this.descriptionString = description;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer-device relationship is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}