package com.breakingjob.userservicems.dto.response;

import java.util.List;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"skills", "projects"})
public class UserProfileResponse {

    private Long id;

    private String firstName;
    private String lastName;

    private String email;
    private String phone;

    private String bio;
    private String profilePicture;
    private String resumeUrl;
    private String location;

    private Boolean skillVerified;

    private List<SkillResponse> skills;
    private List<EducationResponse> education;
    private List<ExperienceResponse> experiences;
    private List<ProjectResponse> projects;
}
