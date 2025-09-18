// JwtAuthenticationFilter.java
package com.service.buffer.client;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            try {
                // Валидируем токен через auth service
                authServiceClient.validateAccessToken(token);
                
                // Получаем client UID из токена
                String clientUid = authServiceClient.getAccessTokenClientUID(token).toString();
                
                // Создаем аутентификацию
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    clientUid, 
                    null, 
                    Collections.emptyList()
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}