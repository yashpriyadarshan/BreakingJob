package com.breakingjob.jobservicems.entity;

import com.breakingjob.jobservicems.type.CurrencyType;
import com.breakingjob.jobservicems.type.EmploymentType;
import com.breakingjob.jobservicems.type.JobType;
import com.breakingjob.jobservicems.type.StatusType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_company_id", columnList = "companyId"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_job_type", columnList = "jobType")
        }
)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    @Enumerated(EnumType.STRING)
    private StatusType status;
    // DRAFT, OPEN, PAUSED, CLOSED

    private String title;

    @Column(length = 5000)
    private String description;

    @Column(length = 5000)
    private String responsibilities;

    @Column(length = 5000)
    private String requirements;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id")
    )
    @Column(name = "skill")
    private List<String> skills;

    private String location;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
    // PART_TIME, FULL_TIME, INTERNSHIP, CONTRACT

    private Integer minExperienceYears;
    // In years

    private Double minSalary;

    private Double maxSalary;

    @Enumerated(EnumType.STRING)
    private CurrencyType currency;
    // INR, USD, EUR

    @Enumerated(EnumType.STRING)
    private JobType jobType;
    // REMOTE, ON_SITE, HYBRID

    private Boolean eddaVerificationRequired;

    private Integer eddaRequiredScore;

    private Integer applicationCount = 0;

    private Integer viewCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (status == null) {
            status = StatusType.DRAFT;
        }

        if (applicationCount == null) {
            applicationCount = 0;
        }

        if (viewCount == null) {
            viewCount = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}