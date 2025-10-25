package com.connection.token.exception;

public class RefreshTokenAddException extends BaseTokenException{
    public RefreshTokenAddException(String tokenString){
        super(tokenString);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: cannot add refresh token";
        return res;
    }
}
