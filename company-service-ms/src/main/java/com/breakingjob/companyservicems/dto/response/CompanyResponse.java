package com.breakingjob.companyservicems.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;

    private String name;

    private String address;

    private String description;

    private String website;

    private String location;

    private String logoUrl;

    private String phone;

    private String email;
}
