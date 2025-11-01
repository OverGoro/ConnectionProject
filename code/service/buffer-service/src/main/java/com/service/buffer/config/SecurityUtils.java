package com.service.buffer.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentClientUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
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

    public static String getCurrentClientUidAsString() {
        return getCurrentClientUid().toString();
    }
}