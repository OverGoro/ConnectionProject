package com.connection.device.exception;

/** . */
public class DeviceAddException extends BaseDeviceException {
    /** . */
    public DeviceAddException(String deviceString) {
        super(deviceString);
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: cannot add device";
        return res;
    }
}
