package com.uni.ethesis.service;

import com.uni.ethesis.data.dto.LoginRequest;
import com.uni.ethesis.data.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}
