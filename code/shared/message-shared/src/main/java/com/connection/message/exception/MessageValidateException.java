package com.connection.message.exception;

/** . */
public class MessageValidateException extends BaseMessageException {
    String descriptionString;

    /** . */
    public MessageValidateException(String messageString, String description) {
        super(messageString);
        this.descriptionString = description;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: message is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}
