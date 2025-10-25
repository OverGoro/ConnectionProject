package com.service.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Login request with email and password")
public class LoginRequest {
    
    @Schema(description = "User email", required = true)
    private String email;
    
    @Schema(description = "User password", required = true)
    private String password;
}