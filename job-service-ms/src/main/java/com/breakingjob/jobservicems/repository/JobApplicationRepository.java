package com.breakingjob.jobservicems.repository;

import com.breakingjob.jobservicems.dto.request.UpdateApplicationStatusRequest;
import com.breakingjob.jobservicems.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndUserId(Long jobId, Long userId);

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByUserId(Long userId);
}