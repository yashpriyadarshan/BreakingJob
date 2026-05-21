package com.breakingjob.jobservicems.dto.request;

import com.breakingjob.jobservicems.type.CurrencyType;
import com.breakingjob.jobservicems.type.EmploymentType;
import com.breakingjob.jobservicems.type.JobType;
import com.breakingjob.jobservicems.type.StatusType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    private Long companyId;

    /*
     * Basic Information
     */
    private String title;

    private String description;

    private String responsibilities;

    private String requirements;

    /*
     * Skills
     */
    private List<String> skills;

    /*
     * Job Details
     */
    private String location;

    private EmploymentType employmentType;

    private JobType jobType;

    /*
     * Experience
     */
    private Integer minExperienceYears;

    /*
     * Salary
     */
    private Double minSalary;

    private Double maxSalary;

    private CurrencyType currency;

    /*
     * AI Interview Configuration
     */
    private Boolean eddaVerificationRequired;

    private Integer eddaRequiredScore;

    /*
     * Job Lifecycle
     */
    private StatusType status;

    private LocalDateTime expiresAt;
}