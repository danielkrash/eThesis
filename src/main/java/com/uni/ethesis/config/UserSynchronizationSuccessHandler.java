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
            String keycloakUserId = null;
            String email = null;
            String firstName = null;
            String lastName = null;
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                // OIDC login
                keycloakUserId = oidcUser.getSubject();
                email = oidcUser.getEmail();
                firstName = oidcUser.getGivenName();
                lastName = oidcUser.getFamilyName();
                log.info("OIDC user login: {}", keycloakUserId);
            } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                // OAuth2 login
                Map<String, Object> attributes = oauth2User.getAttributes();
                keycloakUserId = (String) attributes.get("sub");
                email = (String) attributes.get("email");
                firstName = (String) attributes.get("given_name");
                lastName = (String) attributes.get("family_name");
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

            // Check if user exists in database
            Optional<User> existingUser = userRepository.findById(userId);

            if (existingUser.isEmpty()) {
                // User doesn't exist, create new user
                User newUser = User.builder()
                        .id(userId)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

                userRepository.save(newUser);
                log.info("New user created in database: {} ({})", email, userId);

                // Create Student or Teacher based on roles
                createUserRole(newUser, authorities);
            } else {
                // User exists, optionally update info
                User user = existingUser.get();
                boolean updated = false;

                if (!email.equals(user.getEmail())) {
                    user.setEmail(email);
                    updated = true;
                }
                if (firstName != null && !firstName.equals(user.getFirstName())) {
                    user.setFirstName(firstName);
                    updated = true;
                }
                if (lastName != null && !lastName.equals(user.getLastName())) {
                    user.setLastName(lastName);
                    updated = true;
                }

                if (updated) {
                    userRepository.save(user);
                    log.info("User info updated in database: {} ({})", email, userId);
                } else {
                    log.info("User already exists with current info: {} ({})", email, userId);
                }

                // Ensure user has correct role (in case role changed in Keycloak)
                ensureUserRole(user, authorities);
            }
        } catch (Exception e) {
            log.error("Error during user synchronization", e);
            response.sendRedirect("/error");
            return;
        }

        response.sendRedirect("/dashboard");
    }

    private void createUserRole(User user, Collection<? extends GrantedAuthority> authorities) {
        boolean isStudent = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
        boolean isTeacher = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        if (isStudent) {
            Student student = Student.builder()
                    .user(user)
                    .id(user.getId())
                    .studentType(StudentType.LOCAL) // Default type, can be updated later
                    .build();
            studentRepository.save(student);
            log.info("Student role created for user: {}", user.getEmail());
        }

        if (isTeacher) {
            Teacher teacher = Teacher.builder()
                    .user(user)
                    .id(user.getId())
                    .position(TeacherPosition.TEACHING_ASSISTANT) // Default position, can be updated later
                    .build();
            teacherRepository.save(teacher);
            log.info("Teacher role created for user: {}", user.getEmail());
        }

        if (!isStudent && !isTeacher) {
            log.warn("User has no recognized role (STUDENT/TEACHER): {}", user.getEmail());
        }
    }

    private void ensureUserRole(User user, Collection<? extends GrantedAuthority> authorities) {
        boolean isStudent = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
        boolean isTeacher = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        // Check if student role exists
        if (isStudent && !studentRepository.existsById(user.getId())) {
            Student student = Student.builder()
                    .user(user)
                    .id(user.getId())
                    .studentType(StudentType.LOCAL)
                    .build();
            studentRepository.save(student);
            log.info("Student role added for existing user: {}", user.getEmail());
        }

        // Check if teacher role exists
        if (isTeacher && !teacherRepository.existsById(user.getId())) {
            Teacher teacher = Teacher.builder()
                    .user(user)
                    .id(user.getId())
                    .position(TeacherPosition.TEACHING_ASSISTANT)
                    .build();
            teacherRepository.save(teacher);
            log.info("Teacher role added for existing user: {}", user.getEmail());
        }
    }
}
