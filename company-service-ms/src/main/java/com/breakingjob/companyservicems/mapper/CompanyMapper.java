package com.breakingjob.companyservicems.mapper;

import com.breakingjob.companyservicems.dto.request.CreateCompanyRequest;
import com.breakingjob.companyservicems.dto.request.UpdateCompanyRequest;
import com.breakingjob.companyservicems.dto.response.CompanyResponse;
import com.breakingjob.companyservicems.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CreateCompanyRequest request) {
        return Company.builder()
                .id(request.getId())
                .name(request.getFirstName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    public CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .description(company.getDescription())
                .address(company.getAddress())
                .logoUrl(company.getLogoUrl())
                .location(company.getLocation())
                .build();
    }

    public void toEntity(Company company, UpdateCompanyRequest req) {
        if(req.getAddress() != null) {
            company.setAddress(req.getAddress());
        }
        if(req.getDescription() != null) {
            company.setDescription(req.getDescription());
        }
        if(req.getName() != null) {
            company.setName(req.getName());
        }
        if(req.getLocation() != null) {
            company.setLocation(req.getLocation());
        }
        if(req.getPhone() != null) {
            company.setPhone(req.getPhone());
        }
        if(req.getWebsite() != null) {
            company.setWebsite(req.getWebsite());
        }
    }
}
