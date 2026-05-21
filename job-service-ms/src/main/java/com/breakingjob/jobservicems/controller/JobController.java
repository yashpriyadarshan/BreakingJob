package com.breakingjob.jobservicems.controller;

import com.breakingjob.jobservicems.dto.request.JobRequest;
import com.breakingjob.jobservicems.dto.request.JobSearchRequest;
import com.breakingjob.jobservicems.dto.response.JobResponse;
import com.breakingjob.jobservicems.jwt.JwtUtils;
import com.breakingjob.jobservicems.service.JobService;
import com.breakingjob.jobservicems.type.EmploymentType;
import com.breakingjob.jobservicems.type.JobType;
import com.breakingjob.jobservicems.type.StatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody JobRequest request
    ) {
        Long companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        JobResponse response = jobService.createJob(request, companyId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{jobId}")
    public JobResponse updateJob(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody JobRequest request
    ) {

        Long companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        return jobService.updateJob(jobId, request, companyId);
    }

    @DeleteMapping("/{jobId}")
    public void deleteJob(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long jobId
    ) {

        Long companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        jobService.deleteJob(jobId, companyId);
    }

    @PatchMapping("/{jobId}/status")
    public JobResponse updateJobStatus(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam StatusType status
    ) {

        Long companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);

        return jobService.updateJobStatus(jobId, status, companyId);
    }

    @GetMapping("/company")
    public Page<JobResponse> getCompanyJobs(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return jobService.getJobsByCompany(companyId, pageable);
    }

    // Public / Candidate Endpoints

    @GetMapping("/{jobId}")
    public JobResponse getJobById(
            @PathVariable Long jobId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long companyId = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);
            } catch (Exception e) {
                log.warn("Invalid token provided in getJobById: {}", e.getMessage());
            }
        }
        return jobService.getJobById(jobId, companyId);
    }

    @GetMapping
    public Page<JobResponse> getAllOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return jobService.getAllOpenJobs(pageable);
    }

    @GetMapping("/search")
    public Page<JobResponse> searchJobs(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Long companyId = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                companyId = jwtUtils.getCompanyIdFromJwtToken(authorizationHeader);
            } catch (Exception e) {
                log.warn("Invalid token provided in searchJobs: {}", e.getMessage());
            }
        }

        JobSearchRequest searchRequest = JobSearchRequest.builder()
                .keyword(keyword)
                .location(location)
                .skills(skills)
                .employmentType(employmentType)
                .jobType(jobType)
                .minExperience(minExperience)
                .maxExperience(maxExperience)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return jobService.searchJobs(searchRequest, pageable, companyId);
    }

    /*
     * Analytics
     */

    @PostMapping("/{jobId}/view")
    public void incrementViewCount(
            @PathVariable Long jobId
    ) {
        jobService.incrementViewCount(jobId);
    }
}