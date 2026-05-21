package com.breakingjob.jobservicems.entity;

import com.breakingjob.jobservicems.type.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    
    private Long userId;

    private LocalDateTime appliedAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    // APPLIED, UNDER_REVIEW, REJECTED, HIRED

    @PrePersist
    public void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}