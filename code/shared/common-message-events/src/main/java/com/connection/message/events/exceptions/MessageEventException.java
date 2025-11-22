
package com.connection.message.events.exceptions;

/** . */
public class MessageEventException extends RuntimeException {
    /** . */
    public MessageEventException(String message) {
        super(message);
    }

    /** . */
    public MessageEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
