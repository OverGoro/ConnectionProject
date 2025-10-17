package com.connection.message.client;

import com.connection.message.kafka.TypedAuthKafkaClient;
import com.connection.auth.events.responses.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientJwtKafkaAuthenticationFilter extends OncePerRequestFilter {

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
            log.error("Client authentication failed for token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithToken(String token, HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            cleanToken = cleanToken.replace("Bearer ", "");

            log.info("Validating client token: {}...", cleanToken.substring(0, Math.min(cleanToken.length(), 10)) + "...");

            CompletableFuture<TokenValidationResponse> validationFuture = authKafkaClient.validateToken(cleanToken,
                    "message-service");

            TokenValidationResponse validationResponse = validationFuture
                    .get(10, TimeUnit.SECONDS);

            if (!validationResponse.isValid()) {
                throw new SecurityException("Client token validation failed: " + validationResponse.getError());
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    validationResponse.getClientUid(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated client: {}", validationResponse.getClientUid());

        } catch (java.util.concurrent.TimeoutException e) {
            throw new SecurityException("Client token validation timeout");
        } catch (java.util.concurrent.ExecutionException e) {
            throw new SecurityException("Client token validation error: " + e.getCause().getMessage());
        } catch (Exception e) {
            throw new SecurityException("Client authentication failed: " + e.getMessage());
        }
    }
}