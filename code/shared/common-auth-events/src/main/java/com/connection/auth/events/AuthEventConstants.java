package com.connection.auth.events;

/** . */
public class AuthEventConstants {
    public static final String AUTH_COMMANDS_TOPIC = "auth.commands";
    public static final String AUTH_RESPONSES_TOPIC = "auth.responses";
    public static final String AUTH_EVENTS_TOPIC = "auth.events";
    public static final String USER_EVENTS_TOPIC = "user.events";
    public static final String COMMAND_VALIDATE_TOKEN = "VALIDATE_TOKEN";
    public static final String COMMAND_EXTRACT_CLIENT_UID = "EXTRACT_CLIENT_UID";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";
    public static final String EVENT_USER_CREATED = "USER_CREATED";
    public static final String EVENT_TOKEN_VALIDATED = "TOKEN_VALIDATED";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    
    private AuthEventConstants() {
        // utility class
    }
}