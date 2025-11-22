package com.connection.device.token.exception;

/** . */
public class BaseTokenException extends RuntimeException {
    private final String tokenString;

    /** . */
    public BaseTokenException(String tokenDescription) {
        super("token");
        tokenString = tokenDescription;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "token: " + tokenString;
        return res;
    }
}
