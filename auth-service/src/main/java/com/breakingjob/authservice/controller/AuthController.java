package com.breakingjob.authservice.controller;

import com.breakingjob.authservice.dto.request.LoginRequest;
import com.breakingjob.authservice.dto.request.RegisterRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import com.breakingjob.authservice.service.AuthService;
import com.breakingjob.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token) {
        log.debug("token ${}", token);
        return ResponseEntity.ok(userService.deleteUser(token));
    }
}