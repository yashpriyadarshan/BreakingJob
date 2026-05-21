package com.breakingjob.jobservicems.mapper;

import com.breakingjob.jobservicems.dto.request.JobRequest;
import com.breakingjob.jobservicems.dto.response.JobResponse;
import com.breakingjob.jobservicems.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(JobRequest req) {

        if (req == null) {
            return null;
        }

        return Job.builder()
                .companyId(req.getCompanyId())

                .title(req.getTitle())
                .description(req.getDescription())
                .responsibilities(req.getResponsibilities())
                .requirements(req.getRequirements())

                .skills(req.getSkills())

                .location(req.getLocation())

                .employmentType(req.getEmploymentType())
                .jobType(req.getJobType())

                .minExperienceYears(req.getMinExperienceYears())

                .minSalary(req.getMinSalary())
                .maxSalary(req.getMaxSalary())
                .currency(req.getCurrency())

                .eddaVerificationRequired(req.getEddaVerificationRequired())
                .eddaRequiredScore(req.getEddaRequiredScore())

                .status(req.getStatus())

                .expiresAt(req.getExpiresAt())

                .build();
    }

    /*
     * Entity -> Response req
     */
    public JobResponse toResponse(Job job) {

        if (job == null) {
            return null;
        }

        return JobResponse.builder()

                .id(job.getId())

                .companyId(job.getCompanyId())

                .status(job.getStatus())

                .title(job.getTitle())
                .description(job.getDescription())
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())

                .skills(job.getSkills())

                .location(job.getLocation())

                .employmentType(job.getEmploymentType())
                .jobType(job.getJobType())

                .minExperienceYears(job.getMinExperienceYears())

                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .currency(job.getCurrency())

                .eddaVerificationRequired(job.getEddaVerificationRequired())
                .eddaRequiredScore(job.getEddaRequiredScore())

                .applicationCount(job.getApplicationCount())
                .viewCount(job.getViewCount())

                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .expiresAt(job.getExpiresAt())

                .build();
    }

    /*
     * Update Existing Entity from Request req
     */
    public void updateEntity(Job job, JobRequest req) {

        if (req == null || job == null) {
            return;
        }

        if(req.getTitle() != null)
            job.setTitle(req.getTitle());

        if(req.getDescription() != null)
            job.setDescription(req.getDescription());

        if(req.getResponsibilities() != null)
            job.setResponsibilities(req.getResponsibilities());

        if(req.getRequirements() != null)
            job.setRequirements(req.getRequirements());

        if(req.getSkills() != null)
            job.setSkills(req.getSkills());

        if(req.getLocation() != null)
            job.setLocation(req.getLocation());

        if(req.getEmploymentType() != null)
            job.setEmploymentType(req.getEmploymentType());

        if(req.getJobType() != null)
            job.setJobType(req.getJobType());

        if(req.getMinExperienceYears() != null)
            job.setMinExperienceYears(req.getMinExperienceYears());

        if(req.getMinSalary() != null)
            job.setMinSalary(req.getMinSalary());

        if(req.getMaxSalary() != null)
            job.setMaxSalary(req.getMaxSalary());

        if(req.getCurrency() != null)
            job.setCurrency(req.getCurrency());

        if(req.getEddaVerificationRequired() != null)
            job.setEddaVerificationRequired(req.getEddaVerificationRequired());

        if(req.getEddaRequiredScore() != null)
            job.setEddaRequiredScore(req.getEddaRequiredScore());

        if(req.getStatus() != null)
            job.setStatus(req.getStatus());

        if(req.getExpiresAt() != null)
            job.setExpiresAt(req.getExpiresAt());
    }
}