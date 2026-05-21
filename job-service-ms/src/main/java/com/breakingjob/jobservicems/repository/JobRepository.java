package com.breakingjob.jobservicems.repository;

import com.breakingjob.jobservicems.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> /*  JpaSpecificationExecutor<Job> used for search impl */{
}
