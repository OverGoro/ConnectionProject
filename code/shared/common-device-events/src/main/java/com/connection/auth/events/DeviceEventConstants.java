package com.connection.auth.events;

public class DeviceEventConstants {
    
    // Kafka Topics
    public static final String DEVICE_COMMANDS_TOPIC = "device.commands";
    public static final String DEVICE_RESPONSES_TOPIC = "device.responses";
    public static final String DEVICE_EVENTS_TOPIC = "device.events";
    
    // Command Types
    public static final String COMMAND_GET_DEVICE_BY_UID = "GET_DEVICE_BY_UID";
    public static final String COMMAND_GET_DEVICES_BY_CLIENT_UID = "GET_DEVICES_BY_CLIENT_UID";
    public static final String COMMAND_HEALTH_CHECK = "HEALTH_CHECK";
    
    // Event Types
    public static final String EVENT_DEVICE_CREATED = "DEVICE_CREATED";
    
    private DeviceEventConstants() {
        
    }
}