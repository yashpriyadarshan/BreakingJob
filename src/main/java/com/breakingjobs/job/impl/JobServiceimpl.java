package com.breakingjobs.job.impl;

import com.breakingjobs.job.Job;
import com.breakingjobs.job.JobService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobServiceimpl implements JobService {

    private List<Job> jobs = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public List<Job> findAll() {
        return jobs;
    }

    @Override
    public void create(Job job) {
        job.setId(nextId++);
        jobs.add(job);
    }

    @Override
    public Job findById(Long id) {
        for (Job job : jobs) {
            if (job.getId().equals(id)) {
                return job;
            }
        }
        return null;
    }
}
