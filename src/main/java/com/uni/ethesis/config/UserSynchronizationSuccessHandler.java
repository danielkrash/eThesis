package com.uni.ethesis.config;

import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserSynchronizationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository; // Inject your JPA UserRepository

    public UserSynchronizationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            Map<String, Object> attributes = principal.getAttributes();
            UUID userId = (UUID) attributes.get("sub"); // Or get username from attributes if needed (e.g., "preferred_username")
            String email = (String) attributes.get("email"); // Adjust key based on your Keycloak config
            String firstName = (String) attributes.get("given_name"); // Adjust key
            String lastName = (String) attributes.get("family_name"); // Adjust key

            // Check if user exists in database
            Optional<User> existingUser = userRepository.findById(userId); // Assuming you have findByUsername method

            if (existingUser.isPresent()) {
                // User doesn't exist, create and save
                User newUser = new User();
                newUser.setId(userId);
                userRepository.save(newUser);
                System.out.println("User synchronized and created in database: " + userId);
            } else {
                // User exists, you can optionally update user info here if needed
                System.out.println("User already exists in database: " + userId);
                // Example of optional update:
                // existingUser.setEmail(email);
                // existingUser.setFirstName(firstName);
                // existingUser.setLastName(lastName);
                // userRepository.save(existingUser);
                // System.out.println("User info updated in database: " + username);
            }
        } else {
            System.out.println("Authentication principal is not OAuth2AuthenticatedPrincipal. Cannot synchronize user.");
        }
        response.sendRedirect("/"); // Or your desired success URL
    }
}
