// DeviceAccessTokenNotFoundException.java
package com.connection.device.token.exception;

public class DeviceAccessTokenNotFoundException extends BaseTokenException {
    public DeviceAccessTokenNotFoundException(String tokenString) {
        super(tokenString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device access token not found";
        return res;
    }
}