// DeviceAlreadyExistsException.java
package com.connection.device.exception;

public class DeviceAlreadyExistsException extends BaseDeviceException {
    public DeviceAlreadyExistsException(String deviceUid) {
        super(deviceUid);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device already exists";
        return res;
    }
}