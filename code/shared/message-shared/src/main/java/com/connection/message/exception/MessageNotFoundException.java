package com.connection.message.exception;

public class MessageNotFoundException extends BaseMessageException {
    public MessageNotFoundException(String messageString) {
        super(messageString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: message not found";
        return res;
    }
}