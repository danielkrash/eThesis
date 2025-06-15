package com.uni.ethesis.web.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final LogoutSuccessHandler oidcLogoutSuccessHandler;

    public AuthController(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}");
        this.oidcLogoutSuccessHandler = handler;
    }

    /**
     * Public endpoint to check if the API is running
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "eThesis API is running",
            "authentication", "Keycloak OAuth2/JWT"
        ));
    }

    /**
     * Get current user information from JWT token
     * This endpoint expects the client to already have a JWT token from Keycloak
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No JWT token found. Please authenticate with Keycloak first."));
        }

        try {
            Map<String, Object> claims = jwt.getClaims();
            String userId = jwt.getSubject(); // Keycloak user ID
            String email = jwt.getClaimAsString("email");
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");
            String preferredUsername = jwt.getClaimAsString("preferred_username");

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            Map<String, Object> userInfo = Map.of(
                "id", userId,
                "email", email != null ? email : "",
                "firstName", firstName != null ? firstName : "",
                "lastName", lastName != null ? lastName : "",
                "username", preferredUsername != null ? preferredUsername : "",
                "roles", roles,
                "isStudent", roles.contains("ROLE_STUDENT"),
                "isTeacher", roles.contains("ROLE_TEACHER")
            );

            log.debug("Current user info: {}", userInfo);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            log.error("Error extracting user information from JWT", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to extract user information"));
        }
    }

    /**
     * Information about how to authenticate with this API
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        return ResponseEntity.ok(Map.of(
            "authType", "OAuth2/OpenID Connect",
            "provider", "Keycloak",
            "tokenEndpoint", "http://localhost:8080/realms/graduation-spring-realm/protocol/openid-connect/token",
            "authEndpoint", "http://localhost:8080/realms/graduation-spring-realm/protocol/openid-connect/auth",
            "instructions", Map.of(
                "step1", "Get token from Keycloak token endpoint using username/password",
                "step2", "Include token in Authorization header: 'Bearer <token>'",
                "step3", "Call API endpoints with the token"
            ),
            "example", Map.of(
                "curl", "curl -X POST 'http://localhost:8080/realms/graduation-spring-realm/protocol/openid-connect/token' " +
                       "-H 'Content-Type: application/x-www-form-urlencoded' " +
                       "-d 'grant_type=password&client_id=your-client-id&username=user@example.com&password=password'"
            )
        ));
    }

    /**
     * Logout endpoint for web clients (redirects to Keycloak)
     * For API clients, logout is typically handled client-side by discarding the token
     */
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws ServletException, IOException {
        if (authentication != null) {
            log.info("Logging out user: {}", authentication.getName());
        }
        oidcLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
    }
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
