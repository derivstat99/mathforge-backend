package com.mathforge.service;

import com.mathforge.dto.request.LoginRequest;
import com.mathforge.dto.request.RegisterRequest;
import com.mathforge.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
