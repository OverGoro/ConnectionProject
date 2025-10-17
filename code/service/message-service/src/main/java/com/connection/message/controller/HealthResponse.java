// HealthResponse.java
package com.connection.message.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Ответ health check")
public class HealthResponse {
    
    @Schema(description = "Статус сервиса и зависимостей")
    private final String message;
}