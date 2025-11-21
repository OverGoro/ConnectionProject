package com.connection.token.exception;

/** . */
public class AccessTokenValidateException extends BaseTokenException {
    String descriptionString;

    /** . */
    public AccessTokenValidateException(String tokenString,
            String description) {
        super(tokenString);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: Access token is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
