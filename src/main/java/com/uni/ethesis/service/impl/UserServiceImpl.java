package com.uni.ethesis.service.impl;

import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }
}
