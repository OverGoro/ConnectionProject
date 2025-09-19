package com.connection.client.exception;

public class ClientAlreadyExisistsException extends BaseClientException{
    private final String descriptionString = "client already exists";
    public ClientAlreadyExisistsException(String clientString){
        super(clientString);
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "description: " + descriptionString;
        return res;
    }
}
