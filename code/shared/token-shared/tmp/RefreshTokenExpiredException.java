package com.connection.token.exception;

public class RefreshTokenExpiredException extends BaseTokenException {
    public RefreshTokenExpiredException(String tokenString) {
        super(tokenString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: refresh token is expired";
        return res;
    }
}
