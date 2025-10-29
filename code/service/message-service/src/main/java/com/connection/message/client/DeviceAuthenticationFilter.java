package com.connection.message.client;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;
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
@Component
@RequiredArgsConstructor
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    private final DeviceAuthService deviceAuthClient;
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

            DeviceAccessTokenBLM deviceAccessTokenBLM = deviceAuthClient.validateDeviceAccessToken(cleanToken);

            DeviceTokenBLM deviceTokenBLM = deviceAuthClient.getDeviceToken(deviceAccessTokenBLM.getDeviceTokenUid());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    deviceTokenBLM.getDeviceUid(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE")));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Successfully authenticated device: {}", deviceTokenBLM.getDeviceUid());
        } catch (Exception e) {
            throw new SecurityException("Authentication failed: " + e.getMessage());
        }
    }
}