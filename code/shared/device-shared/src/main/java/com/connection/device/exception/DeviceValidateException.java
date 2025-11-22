
package com.connection.device.exception;

/** . */
public class DeviceValidateException extends BaseDeviceException {
    String descriptionString;

    /** . */
    public DeviceValidateException(String deviceUid, String description) {
        super(deviceUid);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
