package com.uni.ethesis.service;

import com.uni.ethesis.data.dto.UserDto;

import java.util.UUID;

public interface UserService {
    void deleteUserById(UUID id);
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
}
