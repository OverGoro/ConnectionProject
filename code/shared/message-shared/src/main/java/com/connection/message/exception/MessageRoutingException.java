// MessageRoutingException.java
package com.connection.message.exception;

public class MessageRoutingException extends BaseMessageException {
    public MessageRoutingException(String messageString) {
        super(messageString);
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: cannot route message";
        return res;
    }
}