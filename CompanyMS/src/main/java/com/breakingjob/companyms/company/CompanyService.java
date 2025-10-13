package com.breakingjob.companyms.company;

import java.util.List;

public interface CompanyService {
    List<Company> getCompanies();

    void createCompany(Company company);

    boolean updateCompany(Long id, Company company);

    List<Company> findAll();

    boolean deleteCompanyById(Long id);

    Company getCompanyById(Long id);
}
