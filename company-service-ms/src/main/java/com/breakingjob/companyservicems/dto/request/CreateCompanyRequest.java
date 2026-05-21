package com.breakingjob.companyservicems.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest {
    private Long id;

    private String firstName;

    private String email;

    private String phone;
}
