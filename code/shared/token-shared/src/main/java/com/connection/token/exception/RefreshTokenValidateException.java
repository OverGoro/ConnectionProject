package com.connection.token.exception;

/** . */
public class RefreshTokenValidateException extends BaseTokenException {
    String descriptionString;

    /** . */
    public RefreshTokenValidateException(String tokenString,
            String description) {
        super(tokenString);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: refresh token is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
