package com.breakingjob.jobservicems.dto.request;

import com.breakingjob.jobservicems.type.EmploymentType;
import com.breakingjob.jobservicems.type.JobType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchRequest {
    private String keyword;
    private String location;
    private List<String> skills;
    private EmploymentType employmentType;
    private JobType jobType;
    private Integer minExperience;
    private Integer maxExperience;
    private Double minSalary;
    private Double maxSalary;
    private String sortBy; // e.g., "createdAt", "minSalary"
    private String sortDirection; // "asc" or "desc"
}
