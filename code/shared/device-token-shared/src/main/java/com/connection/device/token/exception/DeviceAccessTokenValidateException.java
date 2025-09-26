// DeviceTokenValidateException.java
package com.connection.device.token.exception;

public class DeviceAccessTokenValidateException extends BaseTokenException {
    private final String descriptionString;
    
    public DeviceAccessTokenValidateException(String tokenString, String description) {
        super(tokenString);
        this.descriptionString = description;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: device access token is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}