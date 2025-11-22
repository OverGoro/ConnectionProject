package com.connection.token.exception;

/** . */
public class RefreshTokenNotFoundException extends BaseTokenException {
    /** . */
    public RefreshTokenNotFoundException(String tokenString) {
        super(tokenString);
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: refresh token not found";
        return res;
    }
}
