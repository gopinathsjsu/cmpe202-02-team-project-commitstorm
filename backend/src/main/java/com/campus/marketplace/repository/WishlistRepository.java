package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.entity.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository for Wishlist entity with custom queries

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    
    // Find all wishlist items for a specific user
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find all wishlist items for a specific listing
    List<Wishlist> findByListingIdOrderByCreatedAtDesc(String listingId);
    
    // Check if a specific user has a specific listing in their wishlist
    boolean existsByUserIdAndListingId(String userId, String listingId);
    
    // Find wishlist item by user and listing
    Wishlist findByUserIdAndListingId(String userId, String listingId);
    
    // Delete wishlist item by user and listing
    void deleteByUserIdAndListingId(String userId, String listingId);
    
    // Count wishlist items for a user
    long countByUserId(String userId);
    
    // Count wishlist items for a listing
    long countByListingId(String listingId);
    
    // Find wishlist items with user and listing details loaded
    @Query("SELECT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.user " +
           "LEFT JOIN FETCH w.listing l " +
           "LEFT JOIN FETCH l.seller " +
           "LEFT JOIN FETCH l.category " +
           "WHERE w.userId = :userId " +
           "ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdWithDetails(@Param("userId") String userId);
    
    // Find wishlist items with user and listing details loaded for a specific listing
    @Query("SELECT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.user " +
           "LEFT JOIN FETCH w.listing l " +
           "LEFT JOIN FETCH l.seller " +
           "LEFT JOIN FETCH l.category " +
           "WHERE w.listingId = :listingId " +
           "ORDER BY w.createdAt DESC")
    List<Wishlist> findByListingIdWithDetails(@Param("listingId") String listingId);
    
    // Find wishlist items for a user with pagination
    @Query("SELECT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.user " +
           "LEFT JOIN FETCH w.listing l " +
           "LEFT JOIN FETCH l.seller " +
           "LEFT JOIN FETCH l.category " +
           "WHERE w.userId = :userId " +
           "ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdWithDetailsPaged(@Param("userId") String userId);
}