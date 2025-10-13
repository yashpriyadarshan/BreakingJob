package com.breakingjob.reviewms.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(@RequestParam Long companyId) {
        return ResponseEntity.ok().body(reviewService.getAllReviews(companyId));
    }

    @PostMapping
    public ResponseEntity<String> addReview(@RequestParam Long companyId, @RequestBody Review review) {
        if(reviewService.addReview(companyId, review)) {
            return ResponseEntity.ok().body("Review added Successfully");
        }
        return new ResponseEntity<>("Review added Failed", HttpStatus.NOT_ACCEPTABLE);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        if(review != null) {
            return ResponseEntity.ok().body(review);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long reviewId, @RequestBody Review review) {
        if(reviewService.updateReview(reviewId, review)) {
            return ResponseEntity.ok().body("Review updated Successfully");
        }
        return  new ResponseEntity<>("Review updated Failed", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        if(reviewService.deleteReview(reviewId)) {
            return ResponseEntity.ok().body("Review deleted Successfully");
        }
        return  new ResponseEntity<>("Review deleted Failed", HttpStatus.NOT_FOUND);
    }
}
