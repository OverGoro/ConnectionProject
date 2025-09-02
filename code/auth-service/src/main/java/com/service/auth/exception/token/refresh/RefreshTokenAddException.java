package com.service.auth.exception.token.refresh;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class RefreshTokenAddException extends BaseRefreshTokenException{
    private final String descriptionString = "token add exxception";
    public RefreshTokenAddException(RefreshTokenBLM refreshTokenBLM){
        super(refreshTokenBLM);
    }
    public RefreshTokenAddException(RefreshTokenDALM refreshTokenDALM){
        super(refreshTokenDALM);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
