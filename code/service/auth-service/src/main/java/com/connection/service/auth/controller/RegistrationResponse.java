package com.connection.service.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** . */
@AllArgsConstructor
@Getter
@Setter
@Schema
public class RegistrationResponse {
    private final String message;
    private final String email;
}