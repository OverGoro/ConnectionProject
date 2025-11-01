package com.connection.gateway.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.service.auth.AuthService;
import com.connection.token.model.AccessTokenBLM;
import com.service.device.auth.DeviceAuthService;

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

@Slf4j
@Component("gatewayAuthenticationFilter")
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authClient;
    private final DeviceAuthService deviceAuthClient;

    private static final String CLIENT_AUTH_HEADER = "Authorization";
    private static final String DEVICE_AUTH_HEADER = "X-Device-Authorization"; // Новый заголовок для device
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
            log.info("=== FILTER STARTED ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Client Auth Header: {}", request.getHeader(CLIENT_AUTH_HEADER));
        log.info("Device Auth Header: {}", request.getHeader(DEVICE_AUTH_HEADER));


        String clientAuthHeader = request.getHeader(CLIENT_AUTH_HEADER);
        String deviceAuthHeader = request.getHeader(DEVICE_AUTH_HEADER);

        boolean clientAuthenticated = false;
        boolean deviceAuthenticated = false;

        // Сначала пробуем аутентифицировать клиента
        if (clientAuthHeader != null && clientAuthHeader.startsWith(BEARER_PREFIX)) {
            String jwtToken = clientAuthHeader.substring(BEARER_PREFIX.length());
            clientAuthenticated = authenticateClientWithToken(jwtToken, request);
        }

        // Если клиент не аутентифицирован, пробуем device
        if (!clientAuthenticated && deviceAuthHeader != null && deviceAuthHeader.startsWith(BEARER_PREFIX)) {
            String jwtToken = deviceAuthHeader.substring(BEARER_PREFIX.length());
            deviceAuthenticated = authenticateDeviceWithToken(jwtToken, request);
        }

        // Если ни один токен не прошел аутентификацию, но были предоставлены - возвращаем ошибку
        if ((clientAuthHeader != null && clientAuthHeader.startsWith(BEARER_PREFIX) && !clientAuthenticated) ||
            (deviceAuthHeader != null && deviceAuthHeader.startsWith(BEARER_PREFIX) && !deviceAuthenticated)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateClientWithToken(String token, HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            log.info("Validating client token: {}...", cleanToken.substring(0, Math.min(cleanToken.length(), 20)));

            AccessTokenBLM accessTokenBLM = authClient.validateAccessToken(cleanToken);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    accessTokenBLM.getClientUID(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated client: {}", accessTokenBLM.getClientUID());
            return true;
        } catch (Exception e) {
            log.warn("Client authentication failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean authenticateDeviceWithToken(String token, HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            log.info("Validating device token: {}...", cleanToken.substring(0, Math.min(cleanToken.length(), 20)));

            DeviceAccessTokenBLM deviceAccessTokenBLM = deviceAuthClient.validateDeviceAccessToken(cleanToken);
            DeviceTokenBLM deviceTokenBLM = deviceAuthClient.getDeviceToken(deviceAccessTokenBLM.getDeviceTokenUid());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    deviceTokenBLM.getDeviceUid(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated device: {}", deviceTokenBLM.getDeviceUid());
            return true;
        } catch (Exception e) {
            log.warn("Device authentication failed: {}", e.getMessage());
            return false;
        }
    }
}