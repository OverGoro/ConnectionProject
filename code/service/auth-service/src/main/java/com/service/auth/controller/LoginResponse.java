package com.service.auth.controller;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final Object accessTokenExpiresAt;
    private final Object refreshTokenExpiresAt;
    private final UUID clientUid;
}

