package com.connection.client.exception;

public class BaseClientException extends RuntimeException{
    private final String clientString;

    public BaseClientException(String clientDescription){
        super("client");
        clientString = clientDescription;
    }

    public String toString(){
        String res = super.toString();
        res += "\n" + "client: " + clientString;
        return res;
    }
}
