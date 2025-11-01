package com.connection.message.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;


public class SecurityUtils {

    public static UUID getCurrentClientUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        // Проверяем, что это клиент
        if (!isClientAuthenticated()) {
            throw new SecurityException("Client authentication required");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UUID) {
            return (UUID) principal;
        } else if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                throw new SecurityException("Invalid client UID format in principal");
            }
        } else {
            throw new SecurityException("Unexpected principal type: " + principal.getClass());
        }
    }

    public static UUID getCurrentDeviceUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Device not authenticated");
        }

        // Проверяем, что это устройство
        if (!isDeviceAuthenticated()) {
            throw new SecurityException("Device authentication required");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UUID) {
            return (UUID) principal;
        } else if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                throw new SecurityException("Invalid device UID format in principal");
            }
        } else {
            throw new SecurityException("Unexpected principal type: " + principal.getClass());
        }
    }

    public static boolean isClientAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
    }

    public static boolean isDeviceAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVICE"));
    }

    // Новый метод для проверки любой аутентификации
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    // Новый метод для получения типа аутентификации
    public static String getAuthenticationType() {
        if (isClientAuthenticated()) {
            return "CLIENT";
        } else if (isDeviceAuthenticated()) {
            return "DEVICE";
        } else {
            return "NONE";
        }
    }
}