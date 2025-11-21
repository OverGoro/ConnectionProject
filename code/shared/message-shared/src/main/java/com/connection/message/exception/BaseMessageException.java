package com.connection.message.exception;

/** . */
public class BaseMessageException extends RuntimeException {
    private final String messageString;

    /** . */
    public BaseMessageException(String messageDescription) {
        super("message");
        messageString = messageDescription;
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "message: " + messageString;
        return res;
    }
}
