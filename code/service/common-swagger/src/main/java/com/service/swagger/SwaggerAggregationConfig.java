package com.service.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SwaggerAggregationConfig {

    private final SwaggerProperties swaggerProperties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAPI aggregatedOpenAPI(RestTemplate restTemplate, ObjectMapper objectMapper) {
        OpenAPI aggregatedOpenAPI = new OpenAPI();
        Paths aggregatedPaths = new Paths();
        
        // Set main info
        aggregatedOpenAPI.info(new Info()
                .title("API Gateway - All Microservices")
                .description("Aggregated APIs from all microservices")
                .version("1.0"));
        
        // Add servers
        aggregatedOpenAPI.addServersItem(new Server().url("/").description("API Gateway"));
        
        // Aggregate from all services
        for (Map.Entry<String, SwaggerProperties.ServiceConfig> entry : swaggerProperties.getServices().entrySet()) {
            SwaggerProperties.ServiceConfig serviceConfig = entry.getValue();
            
            try {
                String apiDocsUrl = serviceConfig.getUrl() + serviceConfig.getApiDocsPath();
                log.info("Fetching OpenAPI docs from: {}", apiDocsUrl);
                
                // Get as string and parse manually
                ResponseEntity<String> response = restTemplate.getForEntity(apiDocsUrl, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode apiDocs = objectMapper.readTree(response.getBody());
                    
                    // Extract and merge paths
                    if (apiDocs.has("paths")) {
                        JsonNode pathsNode = apiDocs.get("paths");
                        pathsNode.fields().forEachRemaining(pathEntry -> {
                            String path = pathEntry.getKey();
                            JsonNode pathItem = pathEntry.getValue();
                            
                            try {
                                // Convert JSON to PathItem object
                                io.swagger.v3.oas.models.PathItem pathItemObj = 
                                    objectMapper.convertValue(pathItem, io.swagger.v3.oas.models.PathItem.class);
                                aggregatedPaths.addPathItem(path, pathItemObj);
                                
                                log.debug("Added path: {}", path);
                            } catch (Exception e) {
                                log.warn("Failed to convert path {}: {}", path, e.getMessage());
                            }
                        });
                    }
                    
                    log.info("Successfully aggregated API from {}: {} paths", 
                            serviceConfig.getName(), apiDocs.has("paths") ? apiDocs.get("paths").size() : 0);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch OpenAPI docs from {}: {}", serviceConfig.getName(), e.getMessage());
            }
        }
        
        aggregatedOpenAPI.setPaths(aggregatedPaths);
                aggregatedOpenAPI.components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                            new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

        log.info("Total aggregated paths: {}", aggregatedPaths.size());
        
        return aggregatedOpenAPI;
    }
}