package com.connection.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.connection.message.client.ClientJwtKafkaAuthenticationFilter;
import com.connection.message.client.DeviceJwtKafkaAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientJwtKafkaAuthenticationFilter clientJwtKafkaAuthenticationFilter;
    private final DeviceJwtKafkaAuthenticationFilter deviceJwtKafkaAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        
                        // Разрешаем доступ с любой аутентификацией (клиент ИЛИ устройство)
                        .requestMatchers("/api/v1/messages/**").hasAnyAuthority("ROLE_CLIENT", "ROLE_DEVICE")
                        
                        .anyRequest().denyAll()
                )
                // Оба фильтра будут работать по принципу ИЛИ
                .addFilterBefore(clientJwtKafkaAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(deviceJwtKafkaAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}