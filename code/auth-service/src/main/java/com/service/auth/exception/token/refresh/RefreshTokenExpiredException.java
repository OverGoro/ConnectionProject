package com.service.auth.exception.token.refresh;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class RefreshTokenExpiredException extends BaseRefreshTokenException{
    private final String descriptionString = "token expired";
    public RefreshTokenExpiredException(RefreshTokenBLM refreshTokenBLM){
        super(refreshTokenBLM);
    }
    public RefreshTokenExpiredException(RefreshTokenDALM refreshTokenDALM){
        super(refreshTokenDALM);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
