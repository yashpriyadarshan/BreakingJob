package com.breakingjob.userservicems.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationResponse {

    private Long id;

    private String institution;
    private String degree;
    private String fieldOfStudy;

    private String startDate;
    private String endDate;
}