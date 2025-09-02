package com.service.auth.exception.token.refresh;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class RefreshTokenValidationException extends BaseRefreshTokenException{
    private final String descriptionString = "token not found";
    public RefreshTokenValidationException(RefreshTokenBLM refreshTokenBLM){
        super(refreshTokenBLM);
    }
    public RefreshTokenValidationException(RefreshTokenDALM refreshTokenDALM){
        super(refreshTokenDALM);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
