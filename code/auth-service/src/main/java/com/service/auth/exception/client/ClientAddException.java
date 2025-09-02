package com.service.auth.exception.client;

import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;

public class ClientAddException extends BaseClientException{
    public ClientAddException(ClientBLM clientBLM){
        super(clientBLM);
    }
    public ClientAddException(ClientDALM clientDALM){
        super(clientDALM);
    }
    public String toString(){
        String res = super.toString();
        res += "\n" + "description: cannot add client";
        return res;
    }
}
