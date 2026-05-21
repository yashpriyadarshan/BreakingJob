package com.breakingjob.jobservicems.service;

import com.breakingjob.jobservicems.dto.request.JobRequest;
import com.breakingjob.jobservicems.dto.request.JobSearchRequest;
import com.breakingjob.jobservicems.dto.response.JobResponse;
import com.breakingjob.jobservicems.type.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobResponse createJob(JobRequest request, Long companyId);

    JobResponse updateJob(Long jobId, JobRequest request, Long companyId);

    void deleteJob(Long jobId, Long companyId);

    JobResponse updateJobStatus(Long jobId, StatusType status, Long companyId);

    Page<JobResponse> getJobsByCompany(Long companyId, Pageable pageable);

    JobResponse getJobById(Long jobId, Long companyId);

    Page<JobResponse> getAllOpenJobs(Pageable pageable);

    Page<JobResponse> searchJobs(JobSearchRequest searchRequest, Pageable pageable, Long companyId);

    void incrementViewCount(Long jobId);
}
