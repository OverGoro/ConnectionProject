package com.connection.service.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */

@AllArgsConstructor
@Getter
@Schema
public class HealthResponse {
    private final String status;
    private final String service;
    private final long timestamp;
}

