package com.breakingjob.jobservicems.dto.request;

import com.breakingjob.jobservicems.type.ApplicationStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationStatusRequest {
    private ApplicationStatus status;

}