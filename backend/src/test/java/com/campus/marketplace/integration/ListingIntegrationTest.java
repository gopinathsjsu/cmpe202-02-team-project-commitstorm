package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Listing operations.
 * Tests database constraints, relationships, and business logic.
 */
@Transactional
public class ListingIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testSeller;
    
    @BeforeEach
    void setUp() {
        // Create test seller
        testSeller = new User();
        testSeller.setId("test-seller-001");
        testSeller.setName("Test Seller");
        testSeller.setEmail("test-seller@test.com");
        testSeller.setPassword("password");
        testSeller.setRole(User.UserRole.USER);
        testSeller.setStatus(User.UserStatus.ACTIVE);
        testSeller = userRepository.save(testSeller);
    }
    
    @Test
    void testCreateListing() {
        Listing listing = new Listing();
        listing.setTitle("Test Listing");
        listing.setDescription("Test Description");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(testSeller);
        
        Listing saved = listingRepository.save(listing);
        
        assertNotNull(saved.getId());
        assertEquals("Test Listing", saved.getTitle());
        assertEquals(testSeller.getId(), saved.getSeller().getId());
    }
    
    @Test
    void testListingStatusTransition() {
        Listing listing = new Listing();
        listing.setTitle("Test Listing");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(testSeller);
        listing = listingRepository.save(listing);
        
        // Transition to PENDING
        listing.setStatus(Listing.ListingStatus.PENDING);
        listing = listingRepository.save(listing);
        assertEquals(Listing.ListingStatus.PENDING, listing.getStatus());
        
        // Transition to SOLD
        listing.setStatus(Listing.ListingStatus.SOLD);
        listing = listingRepository.save(listing);
        assertEquals(Listing.ListingStatus.SOLD, listing.getStatus());
    }
    
    @Test
    void testFindBySeller() {
        Listing listing1 = createTestListing("Listing 1");
        Listing listing2 = createTestListing("Listing 2");
        
        var sellerListings = listingRepository.findBySellerId(testSeller.getId());
        
        assertTrue(sellerListings.size() >= 2);
        assertTrue(sellerListings.stream().anyMatch(l -> l.getTitle().equals("Listing 1")));
        assertTrue(sellerListings.stream().anyMatch(l -> l.getTitle().equals("Listing 2")));
    }
    
    @Test
    void testDeleteListing() {
        Listing listing = createTestListing("To Delete");
        String listingId = listing.getId();
        
        listingRepository.deleteById(listingId);
        
        Optional<Listing> deleted = listingRepository.findById(listingId);
        assertFalse(deleted.isPresent());
    }
    
    private Listing createTestListing(String title) {
        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription("Test Description");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(testSeller);
        return listingRepository.save(listing);
    }
}

