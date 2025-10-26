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
        
        // Check if transaction is completed
        Transaction transaction = transactionOpt.get();
        if (transaction.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new RuntimeException("Cannot review incomplete transaction");
        }
        
        // Check if review already exists for this transaction
        if (reviewRepository.findByTransactionId(transactionId).isPresent()) {
            throw new RuntimeException("Review already exists for this transaction");
        }
        
        // Verify that the reviewer is the buyer
        if (!transaction.getBuyer().getId().equals(reviewerId)) {
            throw new RuntimeException("Only the buyer can review this transaction");
        }
        
        // Verify that the seller matches
        if (!transaction.getListing().getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Seller ID does not match transaction");
        }
        
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setTransaction(transaction);
        review.setReviewer(reviewerOpt.get());
        review.setSeller(sellerOpt.get());
        review.setRating(rating);
        review.setComment(comment);
        
        return reviewRepository.save(review);
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
        return reviewRepository.findAll();
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
