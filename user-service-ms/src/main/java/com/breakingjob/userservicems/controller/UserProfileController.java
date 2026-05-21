package com.breakingjob.userservicems.controller;

import com.breakingjob.userservicems.dto.request.*;
import com.breakingjob.userservicems.dto.response.EddaInterviewResponse;
import com.breakingjob.userservicems.dto.response.UserProfileResponse;
import com.breakingjob.userservicems.exception.FileStorageException;
import com.breakingjob.userservicems.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // ──────────────────────────────────────────────
    //  User CRUD Endpoints
    // ──────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(
            @RequestBody CreateUserProfileRequest request) {

        log.info("Creating user profile with id: {}", request.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.createUserProfile(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserProfileRequest request) {

        log.info("Updating user profile with id: {}", id);
        return ResponseEntity.ok(userProfileService.updateUserProfile(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id) {
        log.info("Fetching user profile with id: {}", id);
        return ResponseEntity.ok(userProfileService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        log.info("Deleting user profile with id: {}", id);
        userProfileService.deleteUserProfile(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(HttpServletRequest request) {
        log.info("Fetching authenticated user profile");
        return ResponseEntity.ok(userProfileService.getUserProfile(request));
    }

    // ──────────────────────────────────────────────
    //  Skill Endpoints
    // ──────────────────────────────────────────────

    @PostMapping("/{id}/skills")
    public ResponseEntity<UserProfileResponse> addSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest skillRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.addSkill(id, skillRequest));
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            @PathVariable Long skillId) {

        userProfileService.deleteSkill(id, skillId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────
    //  Experience Endpoints
    // ──────────────────────────────────────────────

    @PostMapping("/{id}/experience")
    public ResponseEntity<UserProfileResponse> addExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest experienceRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.addExperience(id, experienceRequest));
    }

    @DeleteMapping("/{id}/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long id,
            @PathVariable Long experienceId) {

        userProfileService.deleteExperience(id, experienceId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────
    //  Project Endpoints
    // ──────────────────────────────────────────────

    @PostMapping("/{id}/projects")
    public ResponseEntity<UserProfileResponse> addProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest projectRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.addProject(id, projectRequest));
    }

    @DeleteMapping("/{id}/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @PathVariable Long projectId) {

        userProfileService.deleteProject(id, projectId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────
    //  File Upload Endpoints
    // ──────────────────────────────────────────────

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        return ResponseEntity.ok(userProfileService.uploadProfilePicture(id, file));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<UserProfileResponse> uploadResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        log.debug("Resume upload requested for user id: {}", id);

        if (file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        return ResponseEntity.ok(userProfileService.uploadResume(id, file));
    }

    // ──────────────────────────────────────────────
    //  Edda AI Interview Endpoint
    // ──────────────────────────────────────────────

    @PostMapping("/edda/{id}")
    public ResponseEntity<EddaInterviewResponse> createInterviewRoom(@PathVariable Long id) {
        log.info("Creating Edda interview room for user id: {}", id);
        return ResponseEntity.ok(userProfileService.createInterviewRoom(id));
    }
}