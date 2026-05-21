package com.breakingjob.jobservicems.controller;

import com.breakingjob.jobservicems.dto.request.UpdateApplicationStatusRequest;
import com.breakingjob.jobservicems.dto.response.JobApplicationResponse;
import com.breakingjob.jobservicems.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService
            jobApplicationService;

    @PostMapping("/{jobId}")
    public ResponseEntity<String> applyToJob(@PathVariable Long jobId, @RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok(jobApplicationService.applyToJob(jobId, authorizationHeader));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationResponse>> getApplicantsByJobId(@PathVariable Long jobId, @RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok(jobApplicationService.getApplicantsByJobId(jobId, authorizationHeader));
    }

    @GetMapping("/me")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(@RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok(jobApplicationService.getMyApplications(authorizationHeader));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<String> updateApplicationStatus(@PathVariable Long applicationId,
                                                          @RequestHeader("Authorization") String authorizationHeader,
                                                          @RequestBody UpdateApplicationStatusRequest request) {

        jobApplicationService.updateApplicationStatus(applicationId, request, authorizationHeader);

        return ResponseEntity.ok(
                "Application status updated"
        );
    }
}