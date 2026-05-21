package com.breakingjob.companyservicems.repository;

import com.breakingjob.companyservicems.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}