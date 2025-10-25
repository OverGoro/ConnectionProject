// BaseDeviceException.java
package com.connection.device.exception;

public class BaseDeviceException extends RuntimeException {
    private final String deviceUid;

    public BaseDeviceException(String deviceUid) {
        super("device");
        this.deviceUid = deviceUid;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "device: " + deviceUid;
        return res;
    }
}