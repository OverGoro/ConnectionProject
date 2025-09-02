package com.service.auth.exception.token.refresh;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class RefreshTokenAlreadyExisistsException extends BaseRefreshTokenException{
private final String descriptionString = "token already exists";
    public RefreshTokenAlreadyExisistsException(RefreshTokenBLM refreshTokenBLM){
        super(refreshTokenBLM);
    }
    public RefreshTokenAlreadyExisistsException(RefreshTokenDALM refreshTokenDALM){
        super(refreshTokenDALM);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
