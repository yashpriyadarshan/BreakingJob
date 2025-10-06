package com.breakingjobs.review;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews(@PathVariable Long companyId) {
        return ResponseEntity.ok().body(reviewService.getAllReviews(companyId));
    }

    @PostMapping("/reviews")
    public ResponseEntity<String> addReview(@PathVariable Long companyId, @RequestBody Review review) {
        if(reviewService.addReview(companyId, review)) {
            return ResponseEntity.ok().body("Review added Successfully");
        }
        return new ResponseEntity<>("Review added Failed", HttpStatus.NOT_ACCEPTABLE);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long companyId, @PathVariable Long reviewId) {
        Review review = reviewService.getReviewById(companyId, reviewId);
        if(review != null) {
            return ResponseEntity.ok().body(review);
        }
        return ResponseEntity.notFound().build();
    }
}
