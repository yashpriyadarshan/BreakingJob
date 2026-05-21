package com.breakingjob.companyservicems.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyRequest {

    private Long id;

    private String name;

    private String address;

    @Column(length = 1000)
    private String description;

    private String website;

    private String location;

    private String phone;
}
