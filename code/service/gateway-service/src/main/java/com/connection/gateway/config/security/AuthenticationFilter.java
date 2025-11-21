package com.connection.gateway.config.security;

import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.service.auth.AuthService;
import com.connection.token.model.AccessTokenBlm;
import com.service.device.auth.DeviceAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** . */
@Slf4j
@Component("gatewayAuthenticationFilter")
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authClient;
    private final DeviceAuthService deviceAuthClient;

    private static final String CLIENT_AUTH_HEADER = "Authorization";
    private static final String DEVICE_AUTH_HEADER = "X-Device-Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== FILTER STARTED ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Client Auth Header: {}",
                request.getHeader(CLIENT_AUTH_HEADER));
        log.info("Device Auth Header: {}",
                request.getHeader(DEVICE_AUTH_HEADER));

        String clientAuthHeader = request.getHeader(CLIENT_AUTH_HEADER);
        String deviceAuthHeader = request.getHeader(DEVICE_AUTH_HEADER);

        boolean clientAuthenticated =
                authenticateIfValid(clientAuthHeader, request, true);
        boolean deviceAuthenticated = !clientAuthenticated
                && authenticateIfValid(deviceAuthHeader, request, false);

        if (hasFailedAuthentication(clientAuthHeader, clientAuthenticated,
                deviceAuthHeader, deviceAuthenticated)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateIfValid(String authHeader,
            HttpServletRequest request, boolean isClient) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return false;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length());
        return isClient ? authenticateClientWithToken(jwtToken, request)
                : authenticateDeviceWithToken(jwtToken, request);
    }

    private boolean hasFailedAuthentication(String clientAuthHeader,
            boolean clientAuthenticated, String deviceAuthHeader,
            boolean deviceAuthenticated) {
        boolean clientFailed = clientAuthHeader != null
                && clientAuthHeader.startsWith(BEARER_PREFIX)
                && !clientAuthenticated;

        boolean deviceFailed = deviceAuthHeader != null
                && deviceAuthHeader.startsWith(BEARER_PREFIX)
                && !deviceAuthenticated;

        return clientFailed || deviceFailed;
    }

    private boolean authenticateClientWithToken(String token,
            HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            log.info("Validating client token: {}...",
                    cleanToken.substring(0, Math.min(cleanToken.length(), 20)));

            AccessTokenBlm accessTokenBlm =
                    authClient.validateAccessToken(cleanToken);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            accessTokenBlm.getClientUID(), null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_CLIENT")));

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.info("Successfully authenticated client: {}",
                    accessTokenBlm.getClientUID());
            return true;
        } catch (Exception e) {
            log.warn("Client authentication failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean authenticateDeviceWithToken(String token,
            HttpServletRequest request) {
        try {
            String cleanToken = token.trim();
            log.info("Validating device token: {}...",
                    cleanToken.substring(0, Math.min(cleanToken.length(), 20)));

            DeviceAccessTokenBlm deviceAccessTokenBlm =
                    deviceAuthClient.validateDeviceAccessToken(cleanToken);
            DeviceTokenBlm deviceTokenBlm = deviceAuthClient
                    .getDeviceToken(deviceAccessTokenBlm.getDeviceTokenUid());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            deviceTokenBlm.getDeviceUid(), null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_DEVICE")));

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.info("Successfully authenticated device: {}",
                    deviceTokenBlm.getDeviceUid());
            return true;
        } catch (Exception e) {
            log.warn("Device authentication failed: {}", e.getMessage());
            return false;
        }
    }
}
