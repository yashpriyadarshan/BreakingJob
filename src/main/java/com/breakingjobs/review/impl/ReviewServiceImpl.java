package com.breakingjobs.review.impl;

import com.breakingjobs.company.Company;
import com.breakingjobs.company.CompanyRepo;
import com.breakingjobs.company.CompanyService;
import com.breakingjobs.review.Review;
import com.breakingjobs.review.ReviewRepo;
import com.breakingjobs.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepo reviewRepo;
    private final CompanyService companyService;

    @Override
    public List<Review> getAllReviews(Long companyId) {
        return reviewRepo.findByCompanyId(companyId);
    }

    @Override
    public boolean addReview(long companyId, Review review) {
        Company company = companyService.getCompanyById(companyId);
        if (company != null) {
            review.setCompany(company);
            reviewRepo.save(review);
            return true;
        }
        return false;
    }

    @Override
    public Review getReviewById(Long companyId, Long reviewId) {
        List<Review> reviews = reviewRepo.findByCompanyId(companyId);
        return reviews.stream()
                .filter(review -> review.getId().equals(reviewId))
                .findFirst()
                .orElse(null);
    }
}
