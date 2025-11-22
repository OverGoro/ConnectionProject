package com.connection.device.auth.events;

/** . */
public class DeviceAuthEventConstants {

    // Kafka Topics
    public static final String DEVICE_AUTH_COMMANDS_TOPIC =
            "device.auth.commands";
    public static final String DEVICE_AUTH_RESPONSES_TOPIC =
            "device.auth.responses";
    public static final String DEVICE_AUTH_EVENTS_TOPIC = "device.auth.events";
    public static final String DEVICE_TOKEN_EVENTS_TOPIC =
            "device.token.events";

    // Command Types
    public static final String COMMAND_VALIDATE_TOKEN = "VALIDATE_TOKEN";
    public static final String COMMAND_EXTRACT_DEVICE_UID =
            "EXTRACT_DEVICE_UID";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";

    // Event Types
    public static final String EVENT_DEVICE_TOKEN_CREATED =
            "DEVICE_TOKEN_CREATED";
    public static final String EVENT_DEVICE_TOKEN_VALIDATED = "TOKEN_VALIDATED";

    // Token Types
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_DEVICE = "DEVICE";

    private DeviceAuthEventConstants() {
        // utility class
    }
}
