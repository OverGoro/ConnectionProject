package com.service.auth.exception.token.refresh;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class RefreshTokenNotFoundException extends BaseRefreshTokenException{
    private final String descriptionString = "token not found";
    public RefreshTokenNotFoundException(RefreshTokenBLM refreshTokenBLM){
        super(refreshTokenBLM);
    }
    public RefreshTokenNotFoundException(RefreshTokenDALM refreshTokenDALM){
        super(refreshTokenDALM);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
