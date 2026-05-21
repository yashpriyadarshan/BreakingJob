package com.breakingjob.authservice.service;

import com.breakingjob.authservice.dto.request.LoginRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import org.jspecify.annotations.Nullable;

public interface AuthService {

    @Nullable LoginResponse loginUser(LoginRequest loginRequest);
}
