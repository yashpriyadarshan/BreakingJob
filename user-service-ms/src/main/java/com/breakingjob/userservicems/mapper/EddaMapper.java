package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.EddaInterviewRequest;
import com.breakingjob.userservicems.entity.Skill;
import com.breakingjob.userservicems.entity.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class EddaMapper {

    public EddaInterviewRequest toEddaInterviewRequest(UserProfile userProfile) {
        log.debug("Mapping UserProfile to EddaInterviewRequest for user: {}", userProfile.getId());

        return EddaInterviewRequest.builder()
                .userId(userProfile.getId())
                .name(userProfile.getFirstName())
                .skills(extractSkills(userProfile))
                .projects(extractProjects(userProfile))
                .experiences(extractExperiences(userProfile))
                .build();
    }

    private List<String> extractSkills(UserProfile userProfile) {
        if (userProfile.getSkills() == null || userProfile.getSkills().isEmpty()) {
            return Collections.emptyList();
        }

        return userProfile.getSkills()
                .stream()
                .map(Skill::getName)
                .toList();
    }

    private List<String> extractProjects(UserProfile userProfile) {
        if (userProfile.getProjects() == null || userProfile.getProjects().isEmpty()) {
            return Collections.emptyList();
        }

        return userProfile.getProjects()
                .stream()
                .map(project -> project.getTitle() + " : " + project.getDescription())
                .toList();
    }

    private List<String> extractExperiences(UserProfile userProfile) {
        if (userProfile.getExperiences() == null || userProfile.getExperiences().isEmpty()) {
            return Collections.emptyList();
        }

        return userProfile.getExperiences()
                .stream()
                .map(exp -> exp.getRole() + " at " + exp.getCompany())
                .toList();
    }
}