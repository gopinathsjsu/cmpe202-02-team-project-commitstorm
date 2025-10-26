package com.campus.marketplace.service;

import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.WishlistRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WishlistService {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    /**
     * Add a listing to user's wishlist
     * @param userId user id
     * @param listingId listing id
     * @return created Wishlist entity
     * @throws RuntimeException if user or listing not found, or already in wishlist
     */
    public Wishlist addToWishlist(String userId, String listingId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Validate listing exists
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));
        
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new RuntimeException("Listing is already in user's wishlist");
        }
        
        // Create wishlist entry
        Wishlist wishlist = new Wishlist(userId, listingId);
        return wishlistRepository.save(wishlist);
    }
    
    /**
     * Remove a listing from user's wishlist
     * @param userId user id
     * @param listingId listing id
     * @throws RuntimeException if user or listing not found, or not in wishlist
     */
    public void removeFromWishlist(String userId, String listingId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        // Validate listing exists
        if (!listingRepository.existsById(listingId)) {
            throw new RuntimeException("Listing not found with id: " + listingId);
        }
        
        // Check if in wishlist
        if (!wishlistRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new RuntimeException("Listing is not in user's wishlist");
        }
        
        // Remove from wishlist
        wishlistRepository.deleteByUserIdAndListingId(userId, listingId);
    }
    
    /**
     * Get user's wishlist with details
     * @param userId user id
     * @return list of wishlist items with user and listing details
     */
    public List<Wishlist> getUserWishlist(String userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        return wishlistRepository.findByUserIdWithDetails(userId);
    }
    
    /**
     * Get wishlist item by user and listing
     * @param userId user id
     * @param listingId listing id
     * @return Optional wishlist item
     */
    public Optional<Wishlist> getWishlistItem(String userId, String listingId) {
        return Optional.ofNullable(wishlistRepository.findByUserIdAndListingId(userId, listingId));
    }
    
    /**
     * Check if listing is in user's wishlist
     * @param userId user id
     * @param listingId listing id
     * @return true if in wishlist, false otherwise
     */
    public boolean isInWishlist(String userId, String listingId) {
        return wishlistRepository.existsByUserIdAndListingId(userId, listingId);
    }
    
    /**
     * Get count of items in user's wishlist
     * @param userId user id
     * @return count of wishlist items
     */
    public long getWishlistCount(String userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        return wishlistRepository.countByUserId(userId);
    }
    
    /**
     * Get count of users who have a listing in their wishlist
     * @param listingId listing id
     * @return count of users who wishlisted this item
     */
    public long getWishlistCountForListing(String listingId) {
        // Validate listing exists
        if (!listingRepository.existsById(listingId)) {
            throw new RuntimeException("Listing not found with id: " + listingId);
        }
        
        return wishlistRepository.countByListingId(listingId);
    }
    
    /**
     * Get all users who have a specific listing in their wishlist
     * @param listingId listing id
     * @return list of wishlist items with user details
     */
    public List<Wishlist> getWishlistUsersForListing(String listingId) {
        // Validate listing exists
        if (!listingRepository.existsById(listingId)) {
            throw new RuntimeException("Listing not found with id: " + listingId);
        }
        
        return wishlistRepository.findByListingIdWithDetails(listingId);
    }
    
    /**
     * Clear user's entire wishlist
     * @param userId user id
     */
    public void clearUserWishlist(String userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        List<Wishlist> userWishlist = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        wishlistRepository.deleteAll(userWishlist);
    }
}