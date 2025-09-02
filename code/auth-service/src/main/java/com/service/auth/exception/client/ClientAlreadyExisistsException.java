package com.service.auth.exception.client;

import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;

public class ClientAlreadyExisistsException extends BaseClientException{
    private final String descriptionString = "client already exists";
    public ClientAlreadyExisistsException(ClientBLM clientBLM){
        super(clientBLM);
    }
    public ClientAlreadyExisistsException(ClientDALM clientDALM){
        super(clientDALM);
    }
    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
