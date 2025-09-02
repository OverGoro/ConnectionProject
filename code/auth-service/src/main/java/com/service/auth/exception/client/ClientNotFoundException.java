package com.service.auth.exception.client;

import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;

public class ClientNotFoundException extends BaseClientException{
    public ClientNotFoundException(ClientBLM clientBLM){
        super(clientBLM);
    }
    public ClientNotFoundException(ClientDALM clientDALM){
        super(clientDALM);
    }
    public String toString(){
        String res = super.toString();
        res += "\n" + "description: client not found";
        return res;
    }
}
