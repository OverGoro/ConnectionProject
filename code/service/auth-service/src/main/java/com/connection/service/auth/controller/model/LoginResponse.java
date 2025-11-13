package com.connection.service.auth.controller.model;

import java.util.Date;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Login response with tokens")
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;

    private Date accessTokenExpiresAt;  // Используйте конкретный тип
    
    private Date refreshTokenExpiresAt;  // Используйте конкретный тип
    
    private UUID clientUid;
}