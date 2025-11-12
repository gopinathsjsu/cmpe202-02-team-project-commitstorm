package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Wishlist operations.
 * Tests unique constraint (user_id, listing_id) and relationships.
 */
@Transactional
public class WishlistIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    private User testUser;
    private Listing testListing;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId("test-user-001");
        testUser.setName("Test User");
        testUser.setEmail("test-user@test.com");
        testUser.setPassword("password");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);
        
        // Create test seller
        User seller = new User();
        seller.setId("test-seller-001");
        seller.setName("Test Seller");
        seller.setEmail("test-seller@test.com");
        seller.setPassword("password");
        seller.setRole(User.UserRole.USER);
        seller.setStatus(User.UserStatus.ACTIVE);
        seller = userRepository.save(seller);
        
        // Create test listing
        testListing = new Listing();
        testListing.setTitle("Test Listing");
        testListing.setDescription("Test Description");
        testListing.setPrice(new BigDecimal("99.99"));
        testListing.setCondition(Listing.ItemCondition.NEW);
        testListing.setStatus(Listing.ListingStatus.ACTIVE);
        testListing.setSeller(seller);
        testListing = listingRepository.save(testListing);
    }
    
    @Test
    void testAddToWishlist() {
        Wishlist wishlist = new Wishlist(testUser, testListing);
        Wishlist saved = wishlistRepository.save(wishlist);
        
        assertNotNull(saved);
        assertEquals(testUser.getId(), saved.getUserId());
        assertEquals(testListing.getId(), saved.getListingId());
    }
    
    @Test
    void testUniqueConstraint() {
        // Add first time - should succeed
        Wishlist wishlist1 = new Wishlist(testUser, testListing);
        wishlistRepository.save(wishlist1);
        
        // Try to add same item again - should fail due to unique constraint
        assertThrows(Exception.class, () -> {
            Wishlist wishlist2 = new Wishlist(testUser, testListing);
            wishlistRepository.save(wishlist2);
            wishlistRepository.flush(); // Force constraint check
        });
    }
    
    @Test
    void testFindByUserId() {
        Wishlist wishlist = new Wishlist(testUser, testListing);
        wishlistRepository.save(wishlist);
        
        var userWishlist = wishlistRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        
        assertFalse(userWishlist.isEmpty());
        assertTrue(userWishlist.stream()
            .anyMatch(w -> w.getListingId().equals(testListing.getId())));
    }
}

