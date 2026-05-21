package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.SkillRequest;
import com.breakingjob.userservicems.dto.response.SkillResponse;
import com.breakingjob.userservicems.entity.Skill;
import com.breakingjob.userservicems.entity.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillMapper {

    public SkillResponse toResponse(Skill skill) {
        if (skill == null) return null;

        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .build();
    }

    public List<SkillResponse> toResponseList(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) return List.of();

        return skills.stream()
                .map(this::toResponse)
                .toList();
    }

    public Skill toEntity(SkillRequest request, UserProfile user) {
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setUser(user);
        return skill;
    }
}