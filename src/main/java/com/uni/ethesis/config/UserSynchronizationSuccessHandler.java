package com.uni.ethesis.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.enums.StudentType;
import com.uni.ethesis.enums.TeacherPosition;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSynchronizationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            // Handle both OIDC and OAuth2 users
            String keycloakUserId;
            String email;
            String firstName;
            String lastName;
            String universityId;
            String position;
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                // OIDC login
                keycloakUserId = oidcUser.getSubject();
                email = oidcUser.getEmail();
                firstName = oidcUser.getGivenName();
                lastName = oidcUser.getFamilyName();
                universityId = oidcUser.getClaimAsString("faculty_number");
                position = oidcUser.getClaimAsString("position");
                log.info("OIDC user login: {}", keycloakUserId);
            } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                // OAuth2 login
                Map<String, Object> attributes = oauth2User.getAttributes();
                keycloakUserId = (String) attributes.get("sub");
                email = (String) attributes.get("email");
                firstName = (String) attributes.get("given_name");
                lastName = (String) attributes.get("family_name");
                universityId = (String) attributes.get("faculty_number");
                position = (String) attributes.get("position");
                log.info("OAuth2 user login: {}", keycloakUserId);
            } else {
                log.warn("Unknown authentication principal type: {}", authentication.getPrincipal().getClass());
                response.sendRedirect("/");
                return;
            }

            if (keycloakUserId == null || email == null) {
                log.error("Missing required user information from Keycloak");
                response.sendRedirect("/error");
                return;
            }

            // Convert Keycloak user ID to UUID
            UUID userId;
            try {
                userId = UUID.fromString(keycloakUserId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format from Keycloak: {}", keycloakUserId);
                response.sendRedirect("/error");
                return;
            }

            // Use a more robust approach for user management
            User managedUser = createOrUpdateUser(userId, email, firstName, lastName);
            
            // Manage roles separately to avoid cascade conflicts
            manageUserRoles(managedUser, authorities , universityId , position);
            
        } catch (Exception e) {
            log.error("Error during user synchronization", e);
            response.sendRedirect("/error");
            return;
        }

        response.sendRedirect("/dashboard");
    }

    private User createOrUpdateUser(UUID userId, String email, String firstName, String lastName) {
        // First try to find existing user
        Optional<User> existingUser = userRepository.findById(userId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean needsUpdate = false;
            
            // Check if any field needs updating
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                needsUpdate = true;
            }
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                needsUpdate = true;
            }
            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                try {
                    // Use merge instead of save to handle detached entities better
                    User updatedUser = userRepository.saveAndFlush(user);
                    log.info("User info updated in database: {} ({})", email, userId);
                    return updatedUser;
                } catch (Exception e) {
                    log.warn("Failed to update user info, using existing data: {} ({})", email, userId, e);
                    return user;
                }
            } else {
                log.info("User already exists with current info: {} ({})", email, userId);
                return user;
            }
        } else {
            // Create new user
            try {
                User newUser = User.builder()
                        .id(userId)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();
                
                User savedUser = userRepository.saveAndFlush(newUser);
                log.info("New user created in database: {} ({})", email, userId);
                return savedUser;
            } catch (Exception e) {
                log.error("Failed to create new user: {} ({})", email, userId, e);
                // Check if user was created by another thread in the meantime
                Optional<User> retryUser = userRepository.findById(userId);
                if (retryUser.isPresent()) {
                    log.info("User found on retry: {} ({})", email, userId);
                    return retryUser.get();
                } else {
                    throw new RuntimeException("Unable to create or find user", e);
                }
            }
        }
    }

    private void manageUserRoles(User user, Collection<? extends GrantedAuthority> authorities , String universityId , String position) {
        boolean isStudent = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
        boolean isTeacher = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));        // Handle student role
        if (isStudent) {
            if (!studentRepository.existsById(user.getId())) {
                try {
                    Student student = Student.builder()
                            .user(user)
                            .universityId(universityId)
                            .studentType(StudentType.LOCAL)
                            .build();
                    
                    studentRepository.saveAndFlush(student);
                    log.info("Student role created for user: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to create student role for user: {}", user.getEmail(), e);
                }
            }
        }

        // Handle teacher role
        if (isTeacher) {
            if (!teacherRepository.existsById(user.getId())) {
                try {
                    Teacher teacher = Teacher.builder()
                            .user(user)
                            .position(position.isEmpty() ? TeacherPosition.LECTURER : TeacherPosition.valueOf(position.toUpperCase()))
                            .build();
                    
                    teacherRepository.saveAndFlush(teacher);
                    log.info("Teacher role created for user: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to create teacher role for user: {}", user.getEmail(), e);
                }
            }
        }

        if (!isStudent && !isTeacher) {
            log.warn("User has no recognized role (STUDENT/TEACHER): {}", user.getEmail());
        }
    }
}
