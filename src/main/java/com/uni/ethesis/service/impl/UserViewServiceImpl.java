package com.uni.ethesis.service.impl;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.mappers.UserMapper;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserViewService interface.
 * Handles user view-related operations including user data extraction,
 * role processing, and view model creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserViewServiceImpl implements UserViewService {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public UserDto getCurrentUser(Authentication auth) {
        if (auth.getPrincipal() instanceof OAuth2User principal) {
            String id = principal.getAttribute("sub");
            if (id != null) {
                return userService.getUserById(UUID.fromString(id));
            }
        }
        throw new IllegalStateException("Unable to determine current user");
    }

    @Override
    public String getUserRole(Authentication auth) {
        // Check if role is available in OAuth2User attributes
        if (auth.getPrincipal() instanceof OAuth2User principal) {
            // Try to get role from OAuth2 attributes first
            String role = principal.getAttribute("role");
            if (role != null) {
                return role;
            }
        }
        
        // Get all authorities/roles and join them
        String roles = auth.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
            .filter(role -> role.equalsIgnoreCase("STUDENT") || role.equalsIgnoreCase("TEACHER") || role.equalsIgnoreCase("ADMIN"))
            .map(role -> role.toLowerCase())
            .distinct()
            .sorted() // Sort to have consistent order (admin, student, teacher)
            .collect(java.util.stream.Collectors.joining(", "));
        
        return roles.isEmpty() ? "USER" : roles;
    }

    @Override
    public UserViewModel getCurrentUserViewModel(Authentication auth) {
        UserDto currentUser = getCurrentUser(auth);
        UserViewModel userViewModel = userMapper.toViewModel(currentUser);
        userViewModel.setRole(getUserRole(auth));
        return userViewModel;
    }
}
