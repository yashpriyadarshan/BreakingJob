package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.ExperienceRequest;
import com.breakingjob.userservicems.dto.response.ExperienceResponse;
import com.breakingjob.userservicems.entity.Experience;
import com.breakingjob.userservicems.entity.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExperienceMapper {

    public ExperienceResponse toResponse(Experience experience) {
        if (experience == null) return null;

        return ExperienceResponse.builder()
                .id(experience.getId())
                .company(experience.getCompany())
                .role(experience.getRole())
                .description(experience.getDescription())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .build();
    }

    public List<ExperienceResponse> toResponseList(List<Experience> list) {
        if (list == null || list.isEmpty()) return List.of();

        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    public Experience toEntity(ExperienceRequest req, UserProfile user) {
        Experience e = new Experience();
        e.setCompany(req.getCompany());
        e.setRole(req.getRole());
        e.setDescription(req.getDescription());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setUser(user);
        return e;
    }
}