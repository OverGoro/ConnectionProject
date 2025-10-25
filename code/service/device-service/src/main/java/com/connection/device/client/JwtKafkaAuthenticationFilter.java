package com.connection.device.client;

import com.connection.device.kafka.TypedAuthKafkaClient;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.connection.auth.events.responses.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtKafkaAuthenticationFilter extends OncePerRequestFilter {

    private final TypedAuthKafkaClient authKafkaClient;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            authenticateWithToken(jwtToken, request);
        } catch (Exception e) {
            log.error("Authentication failed for token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithToken(String token, HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            cleanToken = cleanToken.replace("Bearer ", "");

            log.info("Validating token: {}...", cleanToken);

            CompletableFuture<TokenValidationResponse> validationFuture = authKafkaClient.validateToken(cleanToken,
                    "device-service");

            TokenValidationResponse validationResponse = validationFuture
                    .get(10, TimeUnit.SECONDS);

            if (!validationResponse.isValid()) {
                throw new SecurityException("Token validation failed: " + validationResponse.getError());
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    validationResponse.getClientUid(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated client: {}", validationResponse.getClientUid());

        } catch (java.util.concurrent.TimeoutException e) {
            throw new SecurityException("Token validation timeout");
        } catch (java.util.concurrent.ExecutionException e) {
            throw new SecurityException("Token validation error: " + e.getCause().getMessage());
        } catch (Exception e) {
            throw new SecurityException("Authentication failed: " + e.getMessage());
        }
    }
}