package com.connection.message.exception;

/** . */
public class MessageAddException extends BaseMessageException {
    /** . */
    public MessageAddException(String messageString) {
        super(messageString);
    }

    /** . */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: cannot add message";
        return res;
    }
}
