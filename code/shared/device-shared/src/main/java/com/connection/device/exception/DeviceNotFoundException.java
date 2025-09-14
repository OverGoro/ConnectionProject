// DeviceNotFoundException.java
package com.connection.device.exception;

public class DeviceNotFoundException extends BaseDeviceException {
    public DeviceNotFoundException(String deviceUid) {
        super(deviceUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device not found";
        return res;
    }
}