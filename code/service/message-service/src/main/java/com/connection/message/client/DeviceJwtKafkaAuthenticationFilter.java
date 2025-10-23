package com.connection.message.client;

import com.connection.message.kafka.TypedDeviceAuthKafkaClient;
import com.connection.device.auth.events.responses.TokenValidationResponse;
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
public class DeviceJwtKafkaAuthenticationFilter extends OncePerRequestFilter {

    private final TypedDeviceAuthKafkaClient deviceAuthKafkaClient;
    private static final String DEVICE_AUTH_HEADER = "Device-Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(DEVICE_AUTH_HEADER);

        // Если нет device заголовка - пропускаем (возможно, это клиент)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String deviceToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            authenticateWithToken(deviceToken, request);
            log.info("Device authentication successful");
        } catch (Exception e) {
            log.error("Device authentication failed for token: {}", e.getMessage());
            // Не прерываем цепочку - возможно, будет успешная client аутентификация
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithToken(String token, HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            cleanToken = cleanToken.replace("Bearer ", "");

            log.info("Validating device token: {}...", cleanToken.substring(0, Math.min(cleanToken.length(), 10)));

            CompletableFuture<TokenValidationResponse> validationFuture = deviceAuthKafkaClient.validateDeviceToken(cleanToken,
                    "message-service");

            TokenValidationResponse validationResponse = validationFuture
                    .get(10, TimeUnit.SECONDS);

            if (!validationResponse.isValid()) {
                throw new SecurityException("Device token validation failed: " + validationResponse.getError());
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    validationResponse.getDeviceUid(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated device: {}", validationResponse.getDeviceUid());

        } catch (java.util.concurrent.TimeoutException e) {
            throw new SecurityException("Device token validation timeout");
        } catch (java.util.concurrent.ExecutionException e) {
            throw new SecurityException("Device token validation error: " + e.getCause().getMessage());
        } catch (Exception e) {
            throw new SecurityException("Device authentication failed: " + e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Проверяем, есть ли клиентская авторизация - если есть, пропускаем device аутентификацию
        String clientAuthHeader = request.getHeader("Authorization");
        return clientAuthHeader != null && clientAuthHeader.startsWith("Bearer ");
    }
}