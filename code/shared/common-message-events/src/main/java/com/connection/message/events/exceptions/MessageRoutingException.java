// MessageRoutingException.java
package com.connection.message.events.exceptions;

public class MessageRoutingException extends MessageEventException {
    
    public MessageRoutingException(String message) {
        super(message);
    }
    
    public MessageRoutingException(String message, Throwable cause) {
        super(message, cause);
    }
}