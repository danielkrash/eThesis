package com.uni.ethesis.service;

import java.util.UUID;

import com.uni.ethesis.data.dto.ProfileUpdateDto;
import com.uni.ethesis.data.dto.UserDto;

public interface UserService {
    void deleteUserById(UUID id);
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    UserDto updateUserProfile(UUID userId, ProfileUpdateDto profileUpdateDto);
    UserDto createOrUpdateUser(String email, String firstName, String lastName);
}
