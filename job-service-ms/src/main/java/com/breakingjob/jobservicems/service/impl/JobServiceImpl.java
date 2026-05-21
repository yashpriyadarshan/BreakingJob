package com.breakingjob.jobservicems.service.impl;

import com.breakingjob.jobservicems.dto.request.JobRequest;
import com.breakingjob.jobservicems.dto.request.JobSearchRequest;
import com.breakingjob.jobservicems.dto.response.JobResponse;
import com.breakingjob.jobservicems.entity.Job;
import com.breakingjob.jobservicems.exception.ResourceNotFoundException;
import com.breakingjob.jobservicems.mapper.JobMapper;
import com.breakingjob.jobservicems.repository.JobRepository;
import com.breakingjob.jobservicems.service.JobService;
import com.breakingjob.jobservicems.type.StatusType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    private Job getJobIfExist(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "Id", jobId));
    }

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, Long companyId) {
        Job job = jobMapper.toEntity(request);
        job.setCompanyId(companyId);

        Job savedJob = jobRepository.save(job);
        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, Long companyId) {
        Job job = getJobIfExist(jobId);

        if(!companyId.equals(job.getCompanyId())) {
            throw new RuntimeException("Job update denied");
        }

        jobMapper.updateEntity(job, request);
        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, Long companyId) {
        Job job = getJobIfExist(jobId);

        if(!companyId.equals(job.getCompanyId())) {
            throw new RuntimeException("Job deletion not allowed");
        }

        jobRepository.delete(job);
    }

    @Override
    @Transactional
    public JobResponse updateJobStatus(Long jobId, StatusType status, Long companyId) {
        Job job = getJobIfExist(jobId);

        if(!companyId.equals(job.getCompanyId())) {
            throw new RuntimeException("Job status update denied");
        }

        job.setStatus(status);
        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public Page<JobResponse> getJobsByCompany(Long companyId, Pageable pageable) {
        Specification<Job> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("companyId"), companyId);

        return jobRepository.findAll(spec, pageable)
                .map(jobMapper::toResponse);
    }

    @Override
    public JobResponse getJobById(Long jobId, Long companyId) {
        Job job = getJobIfExist(jobId);

        // If job is not OPEN, only the owner company can see it
        if (job.getStatus() != StatusType.OPEN) {
            if (companyId == null || !companyId.equals(job.getCompanyId())) {
                throw new ResourceNotFoundException("Job", "Id", jobId);
            }
        }

        return jobMapper.toResponse(job);
    }

    @Override
    public Page<JobResponse> getAllOpenJobs(Pageable pageable) {
        Specification<Job> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusType.OPEN);

        return jobRepository.findAll(spec, pageable)
                .map(jobMapper::toResponse);
    }

    @Override
    public Page<JobResponse> searchJobs(JobSearchRequest searchRequest, Pageable pageable, Long companyId) {
        Specification<Job> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            log.info("search is called");

            // If companyId is provided, they can search all their jobs.
            // Otherwise, only OPEN jobs are returned.
            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), StatusType.OPEN));
            }

            if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
                String searchPattern = "%" + searchRequest.getKeyword().toLowerCase() + "%";
                
                // Search in title, description, and responsibilities
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate respPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("responsibilities")), searchPattern);
                
                predicates.add(criteriaBuilder.or(titlePredicate, descPredicate, respPredicate));
            }

            if (searchRequest.getLocation() != null && !searchRequest.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + searchRequest.getLocation().toLowerCase() + "%"));
            }

            if (searchRequest.getSkills() != null && !searchRequest.getSkills().isEmpty()) {
                List<Predicate> skillPredicates = new ArrayList<>();
                for (String skill : searchRequest.getSkills()) {
                    skillPredicates.add(criteriaBuilder.isMember(skill, root.get("skills")));
                }
                // Match ANY of the skills
                predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
            }

            if (searchRequest.getEmploymentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), searchRequest.getEmploymentType()));
            }

            if (searchRequest.getJobType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("jobType"), searchRequest.getJobType()));
            }

            if (searchRequest.getMinExperience() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minExperienceYears"), searchRequest.getMinExperience()));
            }

            if (searchRequest.getMaxExperience() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minExperienceYears"), searchRequest.getMaxExperience()));
            }

            if (searchRequest.getMinSalary() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minSalary"), searchRequest.getMinSalary()));
            }

            if (searchRequest.getMaxSalary() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxSalary"), searchRequest.getMaxSalary()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Handle sorting
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
            Sort.Direction direction = Sort.Direction.DESC;
            if ("asc".equalsIgnoreCase(searchRequest.getSortDirection())) {
                direction = Sort.Direction.ASC;
            }
            Sort sort = Sort.by(direction, searchRequest.getSortBy());
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        return jobRepository.findAll(spec, pageable)
                .map(jobMapper::toResponse);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long jobId) {
        Job job = getJobIfExist(jobId);
        job.setViewCount(job.getViewCount() + 1);
        jobRepository.save(job);
    }
}
