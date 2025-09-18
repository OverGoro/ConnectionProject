// SecurityConfig.java
package com.service.bufferjsondata.config;

import com.service.bufferjsondata.client.AuthServiceClient;
import com.service.bufferjsondata.client.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final AuthServiceClient authServiceClient;

    public SecurityConfig(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api/buffer-json-data-service/health").permitAll()
                .requestMatchers("/api/buffer-json-data-service/**").permitAll()//.authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(authServiceClient);
    }
}