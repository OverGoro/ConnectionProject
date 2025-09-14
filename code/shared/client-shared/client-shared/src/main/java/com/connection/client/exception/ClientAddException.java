package com.connection.client.exception;

public class ClientAddException extends BaseClientException{
    public ClientAddException(String clientString){
        super(clientString);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: cannot add client";
        return res;
    }
}
