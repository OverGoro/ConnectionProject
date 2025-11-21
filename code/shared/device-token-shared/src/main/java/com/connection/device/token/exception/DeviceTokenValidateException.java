
package com.connection.device.token.exception;

/** . */
public class DeviceTokenValidateException extends BaseTokenException {
    private final String descriptionString;

    /** . */
    public DeviceTokenValidateException(String tokenString,
            String description) {
        super(tokenString);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device token is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
