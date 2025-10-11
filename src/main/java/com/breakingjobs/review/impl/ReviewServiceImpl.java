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

    @Override
    public boolean updateReview(Long companyId, Long reviewId, Review updatedReview) {
        if(companyService.getCompanyById(companyId) != null) {
            updatedReview.setCompany(companyService.getCompanyById(companyId));
            updatedReview.setId(reviewId);
            reviewRepo.save(updatedReview);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteReview(Long companyId, Long reviewId) {
        if(companyService.getCompanyById(companyId) != null && reviewRepo.existsById(reviewId)) {
            Review review  = reviewRepo.findById(reviewId).orElse(null);
            Company company = review.getCompany();
            company.getReviews().remove(review);
            review.setCompany(null);
            companyService.updateCompany(companyId, company);
            reviewRepo.deleteById(reviewId);
            return true;
        }

        return false;
    }
}
