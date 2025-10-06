package com.breakingjobs.company.impl;

import com.breakingjobs.company.Company;
import com.breakingjobs.company.CompanyRepo;
import com.breakingjobs.company.CompanyService;
import com.breakingjobs.job.Job;
import com.breakingjobs.job.JobRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepo companyRepo;

    @Override
    public List<Company> getCompanies() {
        return  companyRepo.findAll();
    }

    @Override
    public void createCompany(Company company) {
        companyRepo.save(company);
    }

    @Override
    public boolean updateCompany(Long id, Company company) {
        Optional<Company> companyOptional = companyRepo.findById(id);
        if(companyOptional.isPresent()) {
            companyRepo.save(company);
            return true;
        }
        return false;
    }

    @Override
    public List<Company> findAll() {
        return  companyRepo.findAll();
    }

    @Override
    public boolean deleteCompanyById(Long id) {
        if(companyRepo.existsById(id)) {
            companyRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepo.findById(id).orElse(null);
    }
}
