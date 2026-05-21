package com.breakingjob.jobservicems.service;

import com.breakingjob.jobservicems.dto.request.UpdateApplicationStatusRequest;
import com.breakingjob.jobservicems.dto.response.JobApplicationResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface JobApplicationService {

    String applyToJob(Long jobId, String authorizationHeader);

    List<JobApplicationResponse> getApplicantsByJobId(Long jobId, String authorizationHeader);

    @Nullable List<JobApplicationResponse> getMyApplications(String authorizationHeader);

    void updateApplicationStatus(Long applicationId,UpdateApplicationStatusRequest request, String authorizationHeader);
}