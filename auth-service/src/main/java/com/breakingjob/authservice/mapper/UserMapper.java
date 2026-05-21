package com.breakingjob.authservice.mapper;

import com.breakingjob.authservice.dto.request.CreateCompanyRequest;
import com.breakingjob.authservice.dto.request.CreateUserProfileRequest;
import com.breakingjob.authservice.dto.request.RegisterRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import com.breakingjob.authservice.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toEntity(RegisterRequest request) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }

    public LoginResponse toLoginResponse(User user, String jwtToken) {

        return LoginResponse.builder()
                .id(user.getId())
                .jwtToken(jwtToken)
                .role(user.getRole())
                .build();
    }

    public CreateUserProfileRequest toUserProfileRequest(RegisterRequest req) {

        return CreateUserProfileRequest.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();
    }

    public CreateCompanyRequest toCompanyRequest(RegisterRequest req) {

        return CreateCompanyRequest.builder()
                .firstName(req.getFirstName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();

    }
}