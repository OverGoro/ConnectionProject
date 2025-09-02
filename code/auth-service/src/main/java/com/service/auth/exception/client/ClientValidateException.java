package com.service.auth.exception.client;

import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;

public class ClientValidateException extends BaseClientException{
    String descriptionString;
    public ClientValidateException(ClientBLM clientBLM, String description){
        super(clientBLM);
        this.descriptionString = description;
    }
    public ClientValidateException(ClientDALM clientDALM, String description){
        super(clientDALM);
        this.descriptionString = description;
    }
    public String toString(){
        String res = super.toString();
        res += "\n" + "description: client is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
