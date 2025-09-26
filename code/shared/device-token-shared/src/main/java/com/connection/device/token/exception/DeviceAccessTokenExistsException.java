// ActiveTokenExistsException.java
package com.connection.device.token.exception;

public class DeviceAccessTokenExistsException extends BaseTokenException {
    public DeviceAccessTokenExistsException(String tokenString) {
        super(tokenString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: active device access token already exists";
        return res;
    }
}