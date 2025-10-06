package com.connection.scheme.events;

public class ConnectionSchemeEventConstants {
    
    // Kafka Topics
    public static final String CONNECTION_SCHEME_COMMANDS_TOPIC = "scheme.commands";
    public static final String CONNECTION_SCHEME_RESPONSES_TOPIC = "scheme.responses";
    public static final String CONNECTION_SCHEME_EVENTS_TOPIC = "scheme.events";
    
    // Command Types
    public static final String COMMAND_GET_CONNECTION_SCHEME_BY_UID = "GET_CONNECTION_SCHEME_BY_UID";
    public static final String COMMAND_GET_CONNECTION_SCHEMES_BY_CLIENT_UID = "GET_CONNECTION_SCHEMES_BY_CLIENT_UID";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";
    
    // Event Types
    public static final String EVENT_CONNECTION_SCHEME_CREATED = "CONNECTION_SCHEME_CREATED";
    
    private ConnectionSchemeEventConstants() {
        
    }
}