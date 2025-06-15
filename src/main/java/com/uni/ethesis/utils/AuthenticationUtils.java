package com.uni.ethesis.utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtils {

    /**
     * Get the current authenticated user's Keycloak ID
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject(); // This is the Keycloak user ID
            return UUID.fromString(subject);
        }
        
        throw new RuntimeException("User not authenticated or not a JWT token");
    }

    /**
     * Get the current authenticated user's email
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        
        throw new RuntimeException("User not authenticated or not a JWT token");
    }

    /**
     * Get the current authenticated user's roles
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if current user is a student
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * Check if current user is a teacher
     */
    public static boolean isTeacher() {
        return hasRole("TEACHER");
    }
}
