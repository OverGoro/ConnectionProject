package com.connection.service.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */

@AllArgsConstructor
@Getter
@Schema
public class ErrorResponse {
    private final String error;
    private final String message;
}

