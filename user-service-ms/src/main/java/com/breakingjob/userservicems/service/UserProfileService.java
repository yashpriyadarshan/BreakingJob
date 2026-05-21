package com.breakingjob.userservicems.service;

import com.breakingjob.userservicems.dto.request.*;
import com.breakingjob.userservicems.dto.response.EddaInterviewResponse;
import com.breakingjob.userservicems.dto.response.UserProfileResponse;
import com.breakingjob.userservicems.exception.FileStorageException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfileResponse createUserProfile(CreateUserProfileRequest request);

    UserProfileResponse getUserById(Long id);

    UserProfileResponse updateUserProfile(Long id, UpdateUserProfileRequest request);

    void deleteUserProfile(Long id, String token);

    UserProfileResponse addSkill(Long userId, SkillRequest request);

    void deleteSkill(Long userId, Long skillId);

    UserProfileResponse addExperience(Long userId, ExperienceRequest request);

    void deleteExperience(Long userId, Long experienceId);

    UserProfileResponse addProject(Long userId, ProjectRequest request);

    void deleteProject(Long userId, Long projectId);

    UserProfileResponse uploadProfilePicture(Long userId, MultipartFile file) throws FileStorageException;

    UserProfileResponse uploadResume(Long userId, MultipartFile file) throws FileStorageException;

    UserProfileResponse getUserProfile(HttpServletRequest request);

    EddaInterviewResponse createInterviewRoom(Long id);
}
