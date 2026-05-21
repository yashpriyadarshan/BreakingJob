package com.breakingjob.companyservicems.service;

import com.breakingjob.companyservicems.dto.request.CreateCompanyRequest;
import com.breakingjob.companyservicems.dto.request.UpdateCompanyRequest;
import com.breakingjob.companyservicems.dto.response.CompanyResponse;
import com.breakingjob.companyservicems.entity.Company;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {
    CompanyResponse createCompany(CreateCompanyRequest request);

    @Nullable CompanyResponse getCompany(Long id);

    @Nullable CompanyResponse updateCompany(UpdateCompanyRequest request);

    void deleteCompany(Long id, String token);

    CompanyResponse uploadLogo(Long userId, MultipartFile file);

    @Nullable CompanyResponse getCompany(HttpServletRequest request);
}
