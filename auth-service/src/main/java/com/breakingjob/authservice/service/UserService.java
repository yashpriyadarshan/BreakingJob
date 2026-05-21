package com.breakingjob.authservice.service;


import com.breakingjob.authservice.dto.request.LoginRequest;
import com.breakingjob.authservice.dto.request.RegisterRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import com.breakingjob.authservice.entity.User;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

public interface UserService {

    @Nullable String createUser(RegisterRequest request);

    @Nullable User getByEmail(String email);

    @Nullable String deleteUser(String token);
}
