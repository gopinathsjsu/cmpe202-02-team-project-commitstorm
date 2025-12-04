package com.campus.marketplace.controller;

import com.campus.marketplace.dto.ReviewDTO;
import com.campus.marketplace.entity.Review;
import com.campus.marketplace.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    /**
     * Create a new review.
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            Review review = reviewService.createReview(
                reviewDTO.getTransactionId(),
                reviewDTO.getReviewerId(),
                reviewDTO.getSellerId(),
                reviewDTO.getRating(),
                reviewDTO.getComment()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewDTO(review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get a review by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable String id) {
        return reviewService.getReviewById(id)
                .map(review -> ResponseEntity.ok(new ReviewDTO(review)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get a review by transaction ID.
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ReviewDTO> getReviewByTransactionId(@PathVariable String transactionId) {
        return reviewService.getReviewByTransactionId(transactionId)
                .map(review -> ResponseEntity.ok(new ReviewDTO(review)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all reviews.
     */
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews().stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews by reviewer ID.
     */
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByReviewer(@PathVariable String reviewerId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByReviewer(reviewerId).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews by seller ID.
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsBySeller(@PathVariable String sellerId) {
        List<ReviewDTO> reviews = reviewService.getReviewsBySeller(sellerId).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews by rating.
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByRating(@PathVariable Integer rating) {
        List<ReviewDTO> reviews = reviewService.getReviewsByRating(rating).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews by seller and rating.
     */
    @GetMapping("/seller/{sellerId}/rating/{rating}")
    public ResponseEntity<List<ReviewDTO>> getReviewsBySellerAndRating(
            @PathVariable String sellerId, 
            @PathVariable Integer rating) {
        List<ReviewDTO> reviews = reviewService.getReviewsBySellerAndRating(sellerId, rating).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get average rating for a seller.
     */
    @GetMapping("/seller/{sellerId}/average-rating")
    public ResponseEntity<Double> getAverageRatingBySeller(@PathVariable String sellerId) {
        Double averageRating = reviewService.getAverageRatingBySeller(sellerId);
        return ResponseEntity.ok(averageRating);
    }
    
    /**
     * Get review count for a seller.
     */
    @GetMapping("/seller/{sellerId}/count")
    public ResponseEntity<Long> getReviewCountBySeller(@PathVariable String sellerId) {
        Long count = reviewService.getReviewCountBySeller(sellerId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Update a review.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable String id, @Valid @RequestBody ReviewDTO reviewDTO) {
        return reviewService.getReviewById(id)
                .map(existingReview -> {
                    existingReview.setRating(reviewDTO.getRating());
                    existingReview.setComment(reviewDTO.getComment());
                    
                    Review updatedReview = reviewService.updateReview(existingReview);
                    return ResponseEntity.ok(new ReviewDTO(updatedReview));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete a review.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
