package com.connection.client.exception;

public class ClientNotFoundException extends BaseClientException {
    public ClientNotFoundException(String clientString) {
        super(clientString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: client not found";
        return res;
    }
}
