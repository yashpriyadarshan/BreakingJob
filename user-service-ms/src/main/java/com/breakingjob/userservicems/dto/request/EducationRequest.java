package com.breakingjob.userservicems.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequest {

    @NotBlank(message = "Institution name is required")
    private String institution;

    @NotBlank(message = "Degree is required")
    private String degree;

    @Size(max = 255, message = "Field of study cannot exceed 255 characters")
    private String fieldOfStudy;

    private String startDate;
    private String endDate;
}