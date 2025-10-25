// TestTopicUtils.java
package com.connection.message.integration;

import java.util.UUID;

public class TestTopicUtils {

    private static final String TEST_SUFFIX = "-test-" + UUID.randomUUID().toString().substring(0, 8);

    public static String generateTestTopic(String baseTopic) {
        String cleanTopic = baseTopic.replaceAll("-test-[a-f0-9]{8}$", "");
        return cleanTopic + TEST_SUFFIX;
    }

    public static String getTestAuthCommandsTopic() {
        return generateTestTopic("auth.commands");
    }

    public static String getTestDeviceAuthCommandsTopic() {
        return generateTestTopic("device.auth.commands");
    }

    public static String getTestDeviceCommandsTopic() {
        return generateTestTopic("device.commands");
    }

    public static String getTestBufferCommandsTopic() {
        return generateTestTopic("buffer.commands");
    }

    public static String getTestConnectionSchemeCommandsTopic() {
        return generateTestTopic("connection.scheme.commands");
    }

    public static String getTestMessageCommandsTopic() {
        return generateTestTopic("message.commands");
    }
}