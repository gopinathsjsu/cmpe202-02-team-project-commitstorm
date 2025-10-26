package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    /**
     * Find reviews by reviewer ID.
     */
    List<Review> findByReviewerId(String reviewerId);
    
    /**
     * Find reviews by seller ID.
     */
    List<Review> findBySellerId(String sellerId);
    
    /**
     * Find reviews by rating.
     */
    List<Review> findByRating(Integer rating);
    
    /**
     * Find review by transaction ID.
     */
    Optional<Review> findByTransactionId(String transactionId);
    
    /**
     * Find reviews by seller ID ordered by creation date.
     */
    @Query("SELECT r FROM Review r WHERE r.seller.id = :sellerId ORDER BY r.createdAt DESC")
    List<Review> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") String sellerId);
    
    /**
     * Find reviews by reviewer ID ordered by creation date.
     */
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId ORDER BY r.createdAt DESC")
    List<Review> findByReviewerIdOrderByCreatedAtDesc(@Param("reviewerId") String reviewerId);
    
    /**
     * Find average rating by seller ID.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId")
    Double findAverageRatingBySellerId(@Param("sellerId") String sellerId);
    
    /**
     * Count reviews by seller ID.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.seller.id = :sellerId")
    Long countBySellerId(@Param("sellerId") String sellerId);
    
    /**
     * Find reviews by seller ID and rating ordered by creation date.
     */
    @Query("SELECT r FROM Review r WHERE r.seller.id = :sellerId AND r.rating = :rating ORDER BY r.createdAt DESC")
    List<Review> findBySellerIdAndRatingOrderByCreatedAtDesc(@Param("sellerId") String sellerId, @Param("rating") Integer rating);
}
