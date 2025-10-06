package com.breakingjobs.review;

import com.breakingjobs.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    List<Review> findByCompanyId(Long companyId);
}
