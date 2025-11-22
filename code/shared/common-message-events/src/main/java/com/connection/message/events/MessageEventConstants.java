
package com.connection.message.events;

/** . */
public class MessageEventConstants {

    // Kafka Topics
    public static final String MESSAGE_COMMANDS_TOPIC = "message.commands";
    public static final String MESSAGE_RESPONSES_TOPIC = "message.responses";
    public static final String MESSAGE_EVENTS_TOPIC = "message.events";

    // Command Types
    public static final String COMMAND_ADD_MESSAGE = "ADD_MESSAGE";
    public static final String COMMAND_GET_MESSAGE = "GET_MESSAGE";
    public static final String COMMAND_GET_MESSAGES_BY_BUFFER =
            "GET_MESSAGES_BY_BUFFER";
    public static final String COMMAND_ROUTE_MESSAGE = "ROUTE_MESSAGE";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";

    // Event Types
    public static final String EVENT_MESSAGE_CREATED = "MESSAGE_CREATED";
    public static final String EVENT_MESSAGE_ROUTED = "MESSAGE_ROUTED";
    public static final String EVENT_MESSAGE_PROCESSED = "MESSAGE_PROCESSED";

    private MessageEventConstants() {
        // utility class
    }
}
