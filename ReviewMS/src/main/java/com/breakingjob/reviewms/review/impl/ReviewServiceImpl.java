package com.breakingjob.reviewms.review.impl;

import com.breakingjob.reviewms.review.Review;
import com.breakingjob.reviewms.review.ReviewRepo;
import com.breakingjob.reviewms.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepo reviewRepo;

    @Override
    public List<Review> getAllReviews(Long companyId) {
        return reviewRepo.findByCompanyId(companyId);
    }

    @Override
    public boolean addReview(Long companyId, Review review) {
        if (companyId != null && review != null) {
            review.setCompanyId(companyId);
            reviewRepo.save(review);
            return true;
        }
        return false;
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepo.findById(reviewId).orElse(null);
    }

    @Override
    public boolean updateReview(Long reviewId, Review updatedReview) {
        Review review = reviewRepo.findById(reviewId).orElse(null);
        if(review != null) {
            updatedReview.setId(reviewId);
            reviewRepo.save(updatedReview);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteReview(Long reviewId) {
        if(reviewRepo.existsById(reviewId)) {
            reviewRepo.deleteById(reviewId);
            return true;
        }

        return false;
    }
}
