package com.connection.token.exception;

/** . */
public class RefreshTokenAlreadyExisistsException extends BaseTokenException {
    private final String descriptionString = "refresh token already exists";

    /** . */
    public RefreshTokenAlreadyExisistsException(String tokenString) {
        super(tokenString);
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
