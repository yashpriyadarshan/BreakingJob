package com.breakingjob.userservicems.service.impl;

import com.breakingjob.userservicems.dto.request.*;
import com.breakingjob.userservicems.dto.response.EddaInterviewResponse;
import com.breakingjob.userservicems.dto.response.UserProfileResponse;
import com.breakingjob.userservicems.entity.Experience;
import com.breakingjob.userservicems.entity.Project;
import com.breakingjob.userservicems.entity.Skill;
import com.breakingjob.userservicems.entity.UserProfile;
import com.breakingjob.userservicems.enums.FileType;
import com.breakingjob.userservicems.exception.ResourceNotFoundException;
import com.breakingjob.userservicems.exception.ResourceOwnershipException;
import com.breakingjob.userservicems.exception.ServiceCommunicationException;
import com.breakingjob.userservicems.jwt.JwtUtils;
import com.breakingjob.userservicems.mapper.*;
import com.breakingjob.userservicems.repository.ExperienceRepository;
import com.breakingjob.userservicems.repository.ProjectRepository;
import com.breakingjob.userservicems.repository.SkillRepository;
import com.breakingjob.userservicems.repository.UserProfileRepository;
import com.breakingjob.userservicems.service.BlobStorageService;
import com.breakingjob.userservicems.service.FileService;
import com.breakingjob.userservicems.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;

    private final UserProfileMapper userProfileMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;
    private final EddaMapper eddaMapper;

    private final FileService fileService;
    private final BlobStorageService blobStorageService;

    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${edda.service.url}")
    private String eddaServiceUrl;

    // ──────────────────────────────────────────────
    //  Private Helpers
    // ──────────────────────────────────────────────

    private UserProfile findUserOrThrow(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Skill findSkillOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
    }

    private Experience findExperienceOrThrow(Long experienceId) {
        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId));
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    /**
     * Builds a response with signed URLs for both profile picture and resume.
     */
    private UserProfileResponse buildResponseWithSignedUrls(UserProfile user) {
        UserProfileResponse response = userProfileMapper.toResponse(user);

        if (user.getProfilePicture() != null) {
            response.setProfilePicture(
                    blobStorageService.generateSignedUrl(user.getProfilePicture(), 100)
            );
        }
        if (user.getResumeUrl() != null) {
            response.setResumeUrl(
                    blobStorageService.generateSignedUrl(user.getResumeUrl(), 30)
            );
        }

        return response;
    }

    // ──────────────────────────────────────────────
    //  CRUD Operations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(CreateUserProfileRequest request) {
        log.info("Creating user profile with id: {}", request.getId());

        UserProfile user = userProfileMapper.toEntity(request);
        userProfileRepository.save(user);

        return userProfileMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        log.debug("Fetching user profile by id: {}", id);

        UserProfile user = findUserOrThrow(id);
        return buildResponseWithSignedUrls(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long id, UpdateUserProfileRequest request) {
        log.debug("Updating user profile with id: {}", id);

        UserProfile user = findUserOrThrow(id);
        userProfileMapper.updateEntity(user, request);
        userProfileRepository.save(user);

        return buildResponseWithSignedUrls(user);
    }

    @Override
    @Transactional
    public void deleteUserProfile(Long id, String token) {
        log.info("Initiating deletion for user profile id: {}", id);

        UserProfile user = findUserOrThrow(id);
        String profilePicUrl = user.getProfilePicture();
        String resumeFileUrl = user.getResumeUrl();

        // Step 1: Delete from Auth service first (if this fails, we abort entirely)
        deleteFromAuthService(id, token);

        // Step 2: Delete from local database
        userProfileRepository.delete(user);
        log.info("User profile deleted from database for id: {}", id);

        // Step 3: Clean up blob storage (best-effort, don't fail the whole operation)
        deleteFileSilently(profilePicUrl, "profile picture");
        deleteFileSilently(resumeFileUrl, "resume");
    }

    private void deleteFromAuthService(Long userId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.startsWith("Bearer ") ? token.substring(7) : token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/api/v1/auth",
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        "Auth Service",
                        "Deletion rejected with status: " + response.getStatusCode()
                );
            }

            log.info("Auth profile deleted successfully for user id: {}", userId);

        } catch (RestClientException e) {
            log.error("Failed to contact Auth service for user id: {}", userId, e);
            throw new ServiceCommunicationException("Auth Service", "Service unreachable", e);
        }
    }

    private void deleteFileSilently(String fileUrl, String fileDescription) {
        if (fileUrl == null) return;

        try {
            fileService.delete(fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete {} from storage: {}", fileDescription, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  Skill Operations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public UserProfileResponse addSkill(Long userId, SkillRequest request) {
        UserProfile user = findUserOrThrow(userId);

        Skill skill = skillMapper.toEntity(request, user);
        skillRepository.save(skill);

        return userProfileMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Skill skill = findSkillOrThrow(skillId);

        if (!skill.getUser().getId().equals(userId)) {
            throw new ResourceOwnershipException("Skill", skillId, userId);
        }

        skillRepository.delete(skill);
    }

    // ──────────────────────────────────────────────
    //  Experience Operations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public UserProfileResponse addExperience(Long userId, ExperienceRequest request) {
        UserProfile user = findUserOrThrow(userId);

        Experience experience = experienceMapper.toEntity(request, user);
        experienceRepository.save(experience);

        return userProfileMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // BUG FIX: was findExperienceExist(userId) — looked up by wrong ID
        Experience experience = findExperienceOrThrow(experienceId);

        // BUG FIX: was comparing userId with experienceId — reversed
        if (!experience.getUser().getId().equals(userId)) {
            throw new ResourceOwnershipException("Experience", experienceId, userId);
        }

        experienceRepository.delete(experience);
    }

    // ──────────────────────────────────────────────
    //  Project Operations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public UserProfileResponse addProject(Long userId, ProjectRequest request) {
        UserProfile user = findUserOrThrow(userId);

        Project project = projectMapper.toEntity(request, user);
        projectRepository.save(project);

        return userProfileMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        // BUG FIX: was `if(existsById)` — inverted logic, threw when user existed
        if (!userProfileRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Project project = findProjectOrThrow(projectId);

        if (!project.getUser().getId().equals(userId)) {
            throw new ResourceOwnershipException("Project", projectId, userId);
        }

        projectRepository.delete(project);
    }

    // ──────────────────────────────────────────────
    //  File Upload Operations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public UserProfileResponse uploadProfilePicture(Long userId, MultipartFile file) {
        log.debug("Uploading profile picture for user id: {}", userId);

        UserProfile user = findUserOrThrow(userId);
        String url = fileService.saveFile(file, FileType.PROFILE);
        user.setProfilePicture(url);

        UserProfileResponse response = userProfileMapper.toResponse(user);
        response.setProfilePicture(blobStorageService.generateSignedUrl(url, 100));
        return response;
    }

    @Override
    @Transactional
    public UserProfileResponse uploadResume(Long userId, MultipartFile file) {
        log.debug("Uploading resume for user id: {}", userId);

        UserProfile user = findUserOrThrow(userId);
        String url = fileService.saveFile(file, FileType.RESUME);
        user.setResumeUrl(url);

        UserProfileResponse response = userProfileMapper.toResponse(user);
        response.setResumeUrl(blobStorageService.generateSignedUrl(url, 30));
        return response;
    }

    // ──────────────────────────────────────────────
    //  JWT-based Profile Retrieval
    // ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(HttpServletRequest request) {
        String jwtToken = jwtUtils.getJwtFromHeader(request);
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);

        log.debug("Fetching profile for authenticated user id: {}", userId);

        UserProfile user = findUserOrThrow(userId);
        return buildResponseWithSignedUrls(user);
    }

    // ──────────────────────────────────────────────
    //  Edda AI Interview Integration
    // ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EddaInterviewResponse createInterviewRoom(Long id) {
        log.info("Creating Edda interview room for user id: {}", id);

        UserProfile user = findUserOrThrow(id);
        EddaInterviewRequest req = eddaMapper.toEddaInterviewRequest(user);

        // Uses the injected RestTemplate bean (was creating new instance before)
        EddaInterviewResponse response = restTemplate.postForObject(
                eddaServiceUrl + "/edda",
                req,
                EddaInterviewResponse.class
        );

        return response;
    }
}