package com.breakingjob.jobservicems.dto.response;

import com.breakingjob.jobservicems.type.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationResponse {
    private Long applicationId;

    private Long jobId;

    private Long userId;

    private ApplicationStatus status;

    private LocalDateTime appliedAt;
}