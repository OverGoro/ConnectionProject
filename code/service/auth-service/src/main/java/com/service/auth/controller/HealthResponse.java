package com.service.auth.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HealthResponse {
    private final String status;
    private final String service;
    private final long timestamp;
}

