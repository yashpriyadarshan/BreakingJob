package com.breakingjob.authservice.service.impl;

import com.breakingjob.authservice.dto.request.LoginRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import com.breakingjob.authservice.entity.User;
import com.breakingjob.authservice.exception.UserNotFoundException;
import com.breakingjob.authservice.jwt.JwtUtils;
import com.breakingjob.authservice.mapper.UserMapper;
import com.breakingjob.authservice.service.AuthService;
import com.breakingjob.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final UserService userService;


    @Override
    public @Nullable LoginResponse loginUser(LoginRequest loginRequest) {
        log.info("Attempting to log in user: {}", loginRequest.getEmail());
        Authentication  authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (UserNotFoundException ex) {
            log.warn("User not found with email: {}", loginRequest.getEmail());
            throw new UserNotFoundException("User not found with email: " + loginRequest.getEmail());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.getByEmail(loginRequest.getEmail());
        String jwtToken = jwtUtils.generateToken(user);

        log.info("User {} logged in successfully", loginRequest.getEmail());
        return userMapper.toLoginResponse(user, jwtToken);
    }
}
