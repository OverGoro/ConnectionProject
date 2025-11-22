
package com.connection.device.token.exception;

/** . */
public class DeviceTokenNotFoundException extends BaseTokenException {
    /** . */
    public DeviceTokenNotFoundException(String tokenString) {
        super(tokenString);
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device token not found";
        return res;
    }
}
