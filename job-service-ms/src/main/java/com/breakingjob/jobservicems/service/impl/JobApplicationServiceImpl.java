package com.breakingjob.jobservicems.service.impl;

import com.breakingjob.jobservicems.dto.request.UpdateApplicationStatusRequest;
import com.breakingjob.jobservicems.dto.response.JobApplicationResponse;
import com.breakingjob.jobservicems.entity.Job;
import com.breakingjob.jobservicems.entity.JobApplication;
import com.breakingjob.jobservicems.jwt.JwtUtils;
import com.breakingjob.jobservicems.repository.JobApplicationRepository;
import com.breakingjob.jobservicems.repository.JobRepository;
import com.breakingjob.jobservicems.service.JobApplicationService;
import com.breakingjob.jobservicems.type.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JwtUtils jwtUtils;

    @Override
    public String applyToJob(Long jobId, String authorizationHeader) {

        Long userId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader); // here user id belongs to candidate who is logged in;

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found"
                        ));

        boolean alreadyApplied = jobApplicationRepository.existsByJobIdAndUserId(jobId, userId);

        if (alreadyApplied) {
            throw new RuntimeException("Already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .jobId(jobId)
                .userId(userId)
                .appliedAt(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .build();

        jobApplicationRepository.save(application);

        return "Applied successfully";
    }

    @Override
    public List<JobApplicationResponse> getApplicantsByJobId(Long jobId, String authorizationHeader) {

        Long companyId =
                jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }

        List<JobApplication> applications = jobApplicationRepository.findByJobId(jobId);

        return applications.stream().map(application -> JobApplicationResponse
                                .builder()
                                .applicationId(application.getId())
                                .jobId(application.getJobId())
                                .userId(application.getUserId())
                                .status(application.getStatus())
                                .appliedAt(application.getAppliedAt())
                                .build())
                                .toList();
    }

    @Override
    public List<JobApplicationResponse> getMyApplications(String authorizationHeader) {

        Long userId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        List<JobApplication> applications = jobApplicationRepository.findByUserId(userId);

        return applications.stream().map(application ->
                        JobApplicationResponse.builder()
                                .applicationId(application.getId())
                                .jobId(application.getJobId())
                                .userId(application.getUserId())
                                .status(application.getStatus())
                                .appliedAt(application.getAppliedAt())
                                .build())
                        .toList();
    }

    @Override
    public void updateApplicationStatus(Long applicationId,UpdateApplicationStatusRequest request, String authorizationHeader) {

        Long companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        JobApplication application = jobApplicationRepository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }

        application.setStatus(request.getStatus());

        jobApplicationRepository.save(application);
    }
}