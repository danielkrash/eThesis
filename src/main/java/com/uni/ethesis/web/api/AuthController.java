package com.uni.ethesis.web.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uni.ethesis.data.dto.LoginRequest;
import com.uni.ethesis.data.dto.LoginResponse;
import com.uni.ethesis.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final LogoutSuccessHandler oidcLogoutSuccessHandler;
    private final AuthService authService;

    public AuthController(ClientRegistrationRepository clientRegistrationRepository, AuthService authService) {
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        // Ensure this URI is registered as a "Valid Redirect URI" in your Keycloak client settings
        handler.setPostLogoutRedirectUri("{baseUrl}"); // Or a more specific URI like "http://localhost:8084/logout-success"
        this.oidcLogoutSuccessHandler = handler; // Assign the configured handler
        this.authService = authService;
    }

    /**
     * Public endpoint, does not require authentication.
     */
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint. No authentication required.");
    }

    /**
     * Provides information about the currently authenticated user (via JWT for API calls).
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUserDetails(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            log.debug("JWT Claims: {}", jwt.getClaims());
            log.debug("JWT Headers: {}", jwt.getHeaders());
            log.debug("JWT Token Value: {}", jwt.getTokenValue());
            Map<String, Object> claims = jwt.getClaims();
            String username = (String) claims.get("preferred_username");
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("username", username, "roles", roles, "token", jwt.getTokenValue()));
        } else if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User) {
            org.springframework.security.oauth2.core.user.DefaultOAuth2User user = (org.springframework.security.oauth2.core.user.DefaultOAuth2User) authentication.getPrincipal();
            return ResponseEntity.ok(Map.of("username", user.getName(), "attributes", user.getAttributes()));
        }
        return ResponseEntity.status(401).body("User not authenticated or JWT not present");
    }

    /**
     * Handles login for API clients.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    /**
     * Handles logout for OIDC clients (e.g., MVC/Thymeleaf sessions).
     * This will invalidate the local session and redirect to Keycloak for global logout.
     * For stateless API clients using JWTs, logout is primarily a client-side action (discarding the token).
     */
    @GetMapping("/logout") // Changed to GET for conventional logout, POST is also fine
    public void oidcLogout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        if (authentication != null) {
            log.info("Logging out user: {}", authentication.getName());
        }
        oidcLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
        // The OidcClientInitiatedLogoutSuccessHandler handles the redirect to Keycloak.
        // No need to return a body if a redirect is happening.
    }

    // --- The following endpoints are typically handled by Keycloak directly --- //

    /*
    @PostMapping("/register")
    public String register() {
        // User registration is typically handled by Keycloak's registration page.
        // Enable registration in your Keycloak realm settings.
        return "User registration is handled by Keycloak.";
    }
    */

    /*
    @PostMapping("/refresh-token")
    public String refreshToken() {
        // Token refresh is an OAuth2 flow where the client (holding a refresh token)
        // makes a request directly to Keycloak's token endpoint.
        // The Spring Boot backend (as a resource server) doesn't typically implement an endpoint for this.
        return "Token refresh is handled by the client with Keycloak's token endpoint.";
    }
    */

    /*
    @PostMapping("/forgot-password")
    public String forgotPassword() {
        // Password recovery is a feature provided by Keycloak.
        // Users should use Keycloak's UI for this.
        return "Forgot password functionality is handled by Keycloak.";
    }
    */

    /*
    @PostMapping("/reset-password")
    public String resetPassword() {
        // Password reset is part of Keycloak's password recovery flow.
        return "Password reset is handled by Keycloak.";
    }
    */

    /*
    @PostMapping("/change-password")
    public String changePassword() {
        // Users can change their password via Keycloak's Account Management console.
        // If an API for this is needed, it would typically involve Keycloak Admin APIs.
        return "Password change is handled by Keycloak Account Management.";
    }
    */

    /*
    @PostMapping("/verify-email")
    public String verifyEmail() {
        // Email verification is a feature of Keycloak, handled via links sent to the user's email.
        return "Email verification is handled by Keycloak.";
    }
    */
}
