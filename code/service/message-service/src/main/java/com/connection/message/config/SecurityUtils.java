package com.connection.message.config;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** . */
public class SecurityUtils {

    private static final String ROLE_CLIENT = "ROLE_CLIENT";
    private static final String ROLE_DEVICE = "ROLE_DEVICE";

    /** . */
    public static UUID getCurrentClientUid() {
        validateClientAuthentication();
        return extractUidFromPrincipal("client");
    }

    /** . */
    public static UUID getCurrentDeviceUid() {
        validateDeviceAuthentication();
        return extractUidFromPrincipal("device");
    }

    /** . */
    public static boolean isClientAuthenticated() {
        return hasAuthority(ROLE_CLIENT);
    }

    /** . */
    public static boolean isDeviceAuthenticated() {
        return hasAuthority(ROLE_DEVICE);
    }

    /** . */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /** . */
    public static String getAuthenticationType() {
        if (isClientAuthenticated()) {
            return "CLIENT";
        } else if (isDeviceAuthenticated()) {
            return "DEVICE";
        } else {
            return "NONE";
        }
    }

    private static void validateClientAuthentication() {
        if (!isClientAuthenticated()) {
            throw new SecurityException("Client authentication required");
        }
    }

    private static void validateDeviceAuthentication() {
        if (!isDeviceAuthenticated()) {
            throw new SecurityException("Device authentication required");
        }
    }

    private static UUID extractUidFromPrincipal(String principalType) {
        Authentication authentication = getAuthentication();
        validateAuthentication(authentication, principalType);

        Object principal = authentication.getPrincipal();
        return convertToUuid(principal, principalType);
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static void validateAuthentication(Authentication authentication,
            String principalType) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException(principalType + " not authenticated");
        }
    }

    private static UUID convertToUuid(Object principal, String principalType) {
        if (principal instanceof UUID) {
            return (UUID) principal;
        } else if (principal instanceof String) {
            return parseUidFromString((String) principal, principalType);
        } else {
            throw new SecurityException(
                    "Unexpected principal type: " + principal.getClass());
        }
    }

    private static UUID parseUidFromString(String uuidString,
            String principalType) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(
                    "Invalid " + principalType + " UID format in principal");
        }
    }

    private static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
