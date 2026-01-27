package com.finance.planner.auth.service;

import com.finance.planner.auth.dto.request.LoginRequest;
import com.finance.planner.auth.dto.request.RegisterRequest;
import com.finance.planner.auth.dto.response.LoginResponse;
import com.finance.planner.auth.dto.response.UserResponse;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(String username);
}
