package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.EducationRequest;
import com.breakingjob.userservicems.dto.response.EducationResponse;
import com.breakingjob.userservicems.entity.Education;
import com.breakingjob.userservicems.entity.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EducationMapper {

    public EducationResponse toResponse(Education e) {
        if (e == null) return null;

        return EducationResponse.builder()
                .id(e.getId())
                .institution(e.getInstitution())
                .degree(e.getDegree())
                .fieldOfStudy(e.getFieldOfStudy())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .build();
    }

    public List<EducationResponse> toResponseList(List<Education> list) {
        if (list == null || list.isEmpty()) return List.of();

        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    public Education toEntity(EducationRequest req, UserProfile user) {
        Education e = new Education();
        e.setInstitution(req.getInstitution());
        e.setDegree(req.getDegree());
        e.setFieldOfStudy(req.getFieldOfStudy());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setUser(user);
        return e;
    }
}