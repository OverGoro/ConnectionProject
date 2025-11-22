package com.connection.client.exception;

/** . */
public class ClientValidateException extends BaseClientException {
    String descriptionString;

    /** . */
    public ClientValidateException(String clientString, String description) {
        super(clientString);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: client is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
