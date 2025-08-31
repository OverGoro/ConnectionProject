package com.service.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.service.auth.model.ClientDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth-service")
public class AuthServiceController {

    private final AuthService authService;

    @PostMapping("ping")
    public void postPing(@RequestBody String entity) {
        log.info("Started postPing");
        authService.authorize(new ClientDTO(UUID.randomUUID(), new Date(), "email@amail.email", "password", "username"));
    }
    
}