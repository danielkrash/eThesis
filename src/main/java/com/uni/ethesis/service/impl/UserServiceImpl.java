package com.uni.ethesis.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.ResourceNotFoundException;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.utils.mappers.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public void deleteUserById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::userToUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(userMapper::userToUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
