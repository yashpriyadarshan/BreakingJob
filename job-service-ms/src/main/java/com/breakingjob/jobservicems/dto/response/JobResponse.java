package com.breakingjob.jobservicems.dto.response;

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
public class JobResponse {

    private Long id;

    private Long companyId;

    /*
     * Status
     */
    private StatusType status;

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
     * AI Interview Config
     */
    private Boolean eddaVerificationRequired;

    private Integer eddaRequiredScore;

    /*
     * Analytics
     */
    private Integer applicationCount;

    private Integer viewCount;

    /*
     * Dates
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;
}