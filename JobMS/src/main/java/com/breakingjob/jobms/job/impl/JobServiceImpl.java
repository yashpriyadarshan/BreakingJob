package com.breakingjob.jobms.job.impl;

import com.breakingjob.jobms.job.Job;
import com.breakingjob.jobms.job.JobRepo;
import com.breakingjob.jobms.job.JobService;
import com.breakingjob.jobms.job.JobWithCompanyDTO;
import com.breakingjob.jobms.job.external.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;

    @Override
    public List<JobWithCompanyDTO> findAll() {
        List<Job> jobs = jobRepo.findAll();

        return jobs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private JobWithCompanyDTO convertToDto(Job job) {
        JobWithCompanyDTO jobWithCompanyDTO = new JobWithCompanyDTO();
        jobWithCompanyDTO.setJob(job);

        RestTemplate restTemplate = new RestTemplate();

        try {
            Company company = restTemplate.getForObject(
                    "http://localhost:8081/companies/" + job.getId(),
                    Company.class);
            jobWithCompanyDTO.setCompany(company);
        } catch (Exception ex) {
            jobWithCompanyDTO.setCompany(null);
        }

        return jobWithCompanyDTO;
    }

    @Override
    public void createJob(Job job) {
        jobRepo.save(job);
    }

    @Override
    public Job findById(Long id) {
        return jobRepo.findById(id).orElse(null);
    }

    @Override
    public boolean deleteJobById(Long id) {
        try {
            if(jobRepo.existsById(id))
                jobRepo.deleteById(id);
            else throw new Exception("Job with id " + id + " not found");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepo.findById(id);
        if(jobOptional.isPresent()) {
            Job job = jobOptional.get();
            job.setTitle(updatedJob.getTitle());
            job.setDescription(updatedJob.getDescription());
            job.setLocation(updatedJob.getLocation());
            job.setMinSalary(updatedJob.getMinSalary());
            job.setMaxSalary(updatedJob.getMaxSalary());
            jobRepo.save(job);
            return true;
        }
        return false;
    }
}
