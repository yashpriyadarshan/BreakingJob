package com.breakingjob.userservicems.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceResponse {

    private Long id;

    private String company;
    private String role;
    private String description;

    private String startDate;
    private String endDate;
}