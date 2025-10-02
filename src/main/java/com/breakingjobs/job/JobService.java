package com.breakingjobs.job;

import java.util.List;

public interface JobService {
    List<Job> findAll();

    void create(Job job);

    Job findById(Long id);
}
