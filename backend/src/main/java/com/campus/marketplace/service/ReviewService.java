package com.campus.marketplace.service;

import com.campus.marketplace.entity.Review;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new review with validation.
     */
    public Review createReview(String transactionId, String reviewerId, String sellerId, Integer rating, String comment) {
        // Validate that entities exist
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        Optional<User> reviewerOpt = userRepository.findById(reviewerId);
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + transactionId);
        }
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("Reviewer not found with id: " + reviewerId);
        }
        if (sellerOpt.isEmpty()) {
            throw new RuntimeException("Seller not found with id: " + sellerId);
        }
        
        Transaction transaction = transactionOpt.get();
        User reviewer = reviewerOpt.get();
        User seller = sellerOpt.get();
        
        // Validate transaction status (only completed transactions can be reviewed)
        if (transaction.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new RuntimeException("Cannot review incomplete transaction");
        }
        
        // Validate that reviewer is the buyer (only buyer can review)
        if (!transaction.getBuyer().getId().equals(reviewerId)) {
            throw new RuntimeException("Only the buyer can review this transaction");
        }
        
        // Validate seller matches the transaction's listing seller
        String actualSellerId = transactionRepository.findSellerIdByTransactionId(transactionId);
        if (actualSellerId == null || !actualSellerId.equals(sellerId)) {
            throw new RuntimeException("Seller ID does not match transaction");
        }
        
        // Check for duplicate reviews (only one review per transaction)
        if (reviewRepository.findByTransactionId(transactionId).isPresent()) {
            throw new RuntimeException("Review already exists for this transaction");
        }
        
        // Create and save review
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setTransaction(transaction);
        review.setReviewer(reviewer);
        review.setSeller(seller);
        review.setRating(rating);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);
        
        // Return the review with eagerly loaded relationships
        return reviewRepository.findByIdWithDetails(savedReview.getId()).orElse(savedReview);
    }
    
    /**
     * Get a review by ID.
     */
    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }
    
    /**
     * Get a review by transaction ID.
     */
    public Optional<Review> getReviewByTransactionId(String transactionId) {
        return reviewRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Get all reviews.
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAllWithDetails();
    }
    
    /**
     * Get reviews by reviewer ID.
     */
    public List<Review> getReviewsByReviewer(String reviewerId) {
        return reviewRepository.findByReviewerIdOrderByCreatedAtDesc(reviewerId);
    }
    
    /**
     * Get reviews by seller ID.
     */
    public List<Review> getReviewsBySeller(String sellerId) {
        return reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }
    
    /**
     * Get reviews by rating.
     */
    public List<Review> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating);
    }
    
    /**
     * Get reviews by seller and rating.
     */
    public List<Review> getReviewsBySellerAndRating(String sellerId, Integer rating) {
        return reviewRepository.findBySellerIdAndRatingOrderByCreatedAtDesc(sellerId, rating);
    }
    
    /**
     * Get average rating for a seller.
     */
    public Double getAverageRatingBySeller(String sellerId) {
        return reviewRepository.findAverageRatingBySellerId(sellerId);
    }
    
    /**
     * Get review count for a seller.
     */
    public Long getReviewCountBySeller(String sellerId) {
        return reviewRepository.countBySellerId(sellerId);
    }
    
    /**
     * Update a review.
     */
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }
    
    /**
     * Delete a review.
     */
    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}
