package com.service.auth.exception.client;

import com.service.auth.exception.BaseException;
import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;

public class BaseClientException extends BaseException{
    private final String uidString;

    public BaseClientException(ClientBLM clientBLM){
        super("client");
        uidString = clientBLM.getUid().toString();
    }

    public BaseClientException(ClientDALM clientDALM){
        super("client");
        uidString = clientDALM.getUid().toString();
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "uid: " + uidString;
        return res;
    }
}
