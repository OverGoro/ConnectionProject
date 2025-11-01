package com.connection.gateway.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter clientAuthenticationFilter;
    // Убираем deviceAuthenticationFilter, т.к. он теперь объединен

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

                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers("/api/v1/device/auth/device-token").hasAuthority("ROLE_CLIENT")
                        .requestMatchers("/api/v1/device/auth/device-token/**").hasAuthority("ROLE_CLIENT")
                        .requestMatchers("/api/v1/device/auth/**").permitAll()

                        .requestMatchers("/api/v1/message/health").permitAll()
                        .requestMatchers("/api/v1/message/messages").hasAnyAuthority("ROLE_CLIENT", "ROLE_DEVICE")
                        .requestMatchers("/api/v1/message/messages/**").hasAnyAuthority("ROLE_CLIENT", "ROLE_DEVICE")
                        
                        .requestMatchers("/api/v1/scheme/health").permitAll()
                        .requestMatchers("/api/v1/scheme/**").hasAuthority("ROLE_CLIENT")
                        
                        .requestMatchers("/api/v1/device/health").permitAll()
                        .requestMatchers("/api/v1/device/**").authenticated()//hasAuthority("ROLE_CLIENT")

                        .requestMatchers("/api/v1/buffer/health").permitAll()
                        .requestMatchers("/api/v1/buffer/**").hasAuthority("ROLE_CLIENT")
                        
                        .anyRequest().denyAll()
                )
                // Оставляем только один фильтр
                .addFilterBefore(clientAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}