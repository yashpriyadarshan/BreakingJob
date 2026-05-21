package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.CreateUserProfileRequest;
import com.breakingjob.userservicems.dto.request.UpdateUserProfileRequest;
import com.breakingjob.userservicems.dto.response.*;
import com.breakingjob.userservicems.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    private final SkillMapper skillMapper;
    private final EducationMapper educationMapper;
    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;

    public UserProfileResponse toResponse(UserProfile user) {
        if (user == null) return null;

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .resumeUrl(user.getResumeUrl())
                .location(user.getLocation())
                .skills(skillMapper.toResponseList(user.getSkills()))
                .education(educationMapper.toResponseList(user.getEducationList()))
                .experiences(experienceMapper.toResponseList(user.getExperiences()))
                .projects(projectMapper.toResponseList(user.getProjects()))
                .skillVerified(user.isSkillVerified())
                .build();
    }

    public UserProfile toEntity(CreateUserProfileRequest req) {
        if (req == null) return null;

        return UserProfile.builder()
                .id(req.getId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();
    }

    public void updateEntity(UserProfile user, UpdateUserProfileRequest req) {
        if (req == null) return;

        if (req.getFirstName() != null) {
            user.setFirstName(req.getFirstName());
        }

        if(req.getLastName() != null) {
            user.setLastName(req.getLastName());
        }

        if(req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }

        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }

        if (req.getLocation() != null) {
            user.setLocation(req.getLocation());
        }

        if(req.getSkillVerified() != null && req.getSkillVerified()) {
            user.setSkillVerified(true);
        }
    }
}