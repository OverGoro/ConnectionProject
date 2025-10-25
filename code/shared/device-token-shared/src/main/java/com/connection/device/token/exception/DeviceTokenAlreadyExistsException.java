// DeviceTokenAlreadyExistsException.java
package com.connection.device.token.exception;

public class DeviceTokenAlreadyExistsException extends BaseTokenException {
    public DeviceTokenAlreadyExistsException(String tokenString) {
        super(tokenString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device token already exists";
        return res;
    }
}