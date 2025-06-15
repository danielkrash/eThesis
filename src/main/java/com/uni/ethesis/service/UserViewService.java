package com.uni.ethesis.service;

import org.springframework.security.core.Authentication;

import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.web.view.model.UserViewModel;

/**
 * Service interface for handling user view-related operations.
 * Provides methods for extracting user data and roles from Authentication
 * and creating view models for presentation layer.
 */
public interface UserViewService {

    /**
     * Gets the current user from the database based on Authentication
     * 
     * @param auth the authentication object containing user information
     * @return UserDto representing the current user
     * @throws IllegalStateException if unable to determine current user
     */
    UserDto getCurrentUser(Authentication auth);

    /**
     * Extracts user roles from Authentication authorities
     * 
     * @param auth the authentication object containing user authorities
     * @return formatted string of user roles (e.g., "admin, teacher" or "student")
     */
    String getUserRole(Authentication auth);

    /**
     * Creates a UserViewModel from the current user's database data and Authentication
     * 
     * @param auth the authentication object containing user information
     * @return UserViewModel with user data and roles for presentation layer
     */
    UserViewModel getCurrentUserViewModel(Authentication auth);
}
