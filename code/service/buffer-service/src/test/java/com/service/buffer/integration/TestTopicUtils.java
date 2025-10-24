package com.service.buffer.integration;

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

    public static String getTestConnectionSchemeCommandsTopic() {
        return generateTestTopic("connection-scheme.commands");
    }

    public static String getTestDeviceCommandsTopic() {
        return generateTestTopic("device.commands");
    }
}