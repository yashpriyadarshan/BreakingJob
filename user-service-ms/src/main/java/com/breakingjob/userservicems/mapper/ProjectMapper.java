package com.breakingjob.userservicems.mapper;

import com.breakingjob.userservicems.dto.request.ProjectRequest;
import com.breakingjob.userservicems.dto.response.ProjectResponse;
import com.breakingjob.userservicems.entity.Project;
import com.breakingjob.userservicems.entity.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project p) {
        if (p == null) return null;

        return ProjectResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .githubLink(p.getGithubLink())
                .liveLink(p.getLiveLink())
                .build();
    }

    public List<ProjectResponse> toResponseList(List<Project> list) {
        if (list == null || list.isEmpty()) return List.of();

        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    public Project toEntity(ProjectRequest req, UserProfile user) {
        Project p = new Project();
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        p.setGithubLink(req.getGithubLink());
        p.setLiveLink(req.getLiveLink());
        p.setUser(user);
        return p;
    }
}
