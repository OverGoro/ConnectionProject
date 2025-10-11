package com.service.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    
    private Map<String, ServiceConfig> services;
    
    @Data
    public static class ServiceConfig {
        private String name;
        private String url;
        private String apiDocsPath = "/v3/api-docs";
    }
}