package com.breakingjob.authservice.service.impl;

import com.breakingjob.authservice.dto.request.CreateCompanyRequest;
import com.breakingjob.authservice.dto.request.CreateUserProfileRequest;
import com.breakingjob.authservice.dto.request.LoginRequest;
import com.breakingjob.authservice.dto.request.RegisterRequest;
import com.breakingjob.authservice.dto.response.LoginResponse;
import com.breakingjob.authservice.entity.User;
import com.breakingjob.authservice.exception.UserAlreadyExistsException;
import com.breakingjob.authservice.exception.UserNotFoundException;
import com.breakingjob.authservice.jwt.JwtUtils;
import com.breakingjob.authservice.mapper.UserMapper;
import com.breakingjob.authservice.repository.UserRepository;
import com.breakingjob.authservice.service.UserService;
import com.breakingjob.authservice.type.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${company.service.url}")
    private String companyServiceUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public User getByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }

    @Override
    @Transactional
    public String createUser(RegisterRequest request) {
        log.info("Attempting to create a new user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User registration failed: email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email is already registered");
        }
        log.debug(request.getRole().toString());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info(user.getRole() + " {} saved to the database", user.getEmail());

        try {
            if (request.getRole() == RoleType.CANDIDATE) {
                CreateUserProfileRequest profileRequest = userMapper.toUserProfileRequest(request);
                profileRequest.setId(user.getId());

                log.info("Creating user profile for user ID: {}", user.getId());
                ResponseEntity<Object> response = restTemplate.postForEntity(userServiceUrl, profileRequest, Object.class);

                if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                    log.info("Successfully created user profile for user ID: {}", user.getId());
                } else {
                    log.warn("Failed to create profile. Status: {}", response.getStatusCode());
                }

            } else if (request.getRole() == RoleType.RECRUITER) {
                CreateCompanyRequest companyRequest = userMapper.toCompanyRequest(request);
                companyRequest.setId(user.getId());

                log.info("Creating company profile for user ID: {}", user.getId());
                ResponseEntity<Object> response = restTemplate.postForEntity(companyServiceUrl, companyRequest, Object.class);
                if(response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                    log.info("Successfully created company profile for user ID: {}", user.getId());
                } else {
                    log.info("Failed to create company profile. Status: {}", response.getStatusCode());
                }
            }
        } catch (RestClientException e) {
            log.error("Failed to create profile for user ID: {}. Error: {}", user.getId(), e.getMessage());
            throw new IllegalStateException("Failed to create user profile. Please try again later.", e);
        }

        log.info("User {} registered successfully", user.getEmail());
        return "User registered successfully";
    }

    @Override
    public @Nullable String deleteUser(String token) {
        log.debug("token ${}", token);
        Long userId = jwtUtils.getIdFromJwtToken(token.substring(7));
        log.debug("user id ${}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        if(user != null) {
            userRepository.delete(user);
        }
        log.warn("User with id: " + userId + " deleted");
        return "User Deleted Successful";
    }
}
