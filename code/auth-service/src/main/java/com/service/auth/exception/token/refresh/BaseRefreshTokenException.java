package com.service.auth.exception.token.refresh;

import com.service.auth.exception.BaseException;
import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;

public class BaseRefreshTokenException extends BaseException{
    private final String tokenUidString;
    private final String clientUidString;
    public BaseRefreshTokenException(RefreshTokenBLM refreshTokenBLM){
        super("refresh_token");
        
        tokenUidString = refreshTokenBLM.getUid().toString();
        clientUidString = refreshTokenBLM.getClientUID().toString();
        
    }
    public BaseRefreshTokenException(RefreshTokenDALM refreshTokenDALM){
        super("refresh_token");
        
        tokenUidString = refreshTokenDALM.getUid().toString();
        clientUidString = refreshTokenDALM.getClientUID().toString();
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "tokenUid: " + tokenUidString;
        res += "\n" + "clientUid: " + clientUidString;
        return res;
    }
}
