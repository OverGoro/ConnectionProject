// BufferEventConstants.java
package com.connection.buffer.events;

public class BufferEventConstants {
    
    // Kafka Topics
    public static final String BUFFER_COMMANDS_TOPIC = "buffer.commands";
    public static final String BUFFER_RESPONSES_TOPIC = "buffer.responses";
    public static final String BUFFER_EVENTS_TOPIC = "buffer.events";
    
    // Command Types
    public static final String COMMAND_GET_BUFFER_BY_UID = "GET_BUFFER_BY_UID";
    public static final String COMMAND_GET_BUFFERS_BY_CLIENT_UID = "GET_BUFFERS_BY_CLIENT_UID";
    public static final String COMMAND_GET_BUFFERS_BY_DEVICE_UID = "GET_BUFFERS_BY_DEVICE_UID";
    public static final String COMMAND_GET_BUFFERS_BY_CONNECTION_SCHEME_UID = "GET_BUFFERS_BY_CONNECTION_SCHEME_UID";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";
    
    // Event Types
    public static final String EVENT_BUFFER_CREATED = "BUFFER_CREATED";
    public static final String EVENT_BUFFER_UPDATED = "BUFFER_UPDATED";
    public static final String EVENT_BUFFER_DELETED = "BUFFER_DELETED";
    
    private BufferEventConstants() {
        // utility class
    }
}