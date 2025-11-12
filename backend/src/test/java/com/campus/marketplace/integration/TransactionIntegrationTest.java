package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Transaction operations.
 * Tests transaction lifecycle and listing status synchronization.
 */
@Transactional
public class TransactionIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User seller;
    private User buyer;
    private Listing listing;
    
    @BeforeEach
    void setUp() {
        // Create seller
        seller = new User();
        seller.setId("test-seller-001");
        seller.setName("Test Seller");
        seller.setEmail("test-seller@test.com");
        seller.setPassword("password");
        seller.setRole(User.UserRole.USER);
        seller.setStatus(User.UserStatus.ACTIVE);
        seller = userRepository.save(seller);
        
        // Create buyer
        buyer = new User();
        buyer.setId("test-buyer-001");
        buyer.setName("Test Buyer");
        buyer.setEmail("test-buyer@test.com");
        buyer.setPassword("password");
        buyer.setRole(User.UserRole.USER);
        buyer.setStatus(User.UserStatus.ACTIVE);
        buyer = userRepository.save(buyer);
        
        // Create listing
        listing = new Listing();
        listing.setTitle("Test Listing");
        listing.setDescription("Test Description");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(seller);
        listing = listingRepository.save(listing);
    }
    
    @Test
    void testCreateTransaction() {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(new BigDecimal("99.99"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        Transaction saved = transactionRepository.save(transaction);
        
        assertNotNull(saved.getId());
        assertEquals(listing.getId(), saved.getListing().getId());
        assertEquals(buyer.getId(), saved.getBuyer().getId());
        assertEquals(Transaction.TransactionStatus.PENDING, saved.getStatus());
    }
    
    @Test
    void testTransactionStatusTransition() {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(new BigDecimal("99.99"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction = transactionRepository.save(transaction);
        
        // Transition to COMPLETED
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        listing.setStatus(Listing.ListingStatus.SOLD);
        transaction = transactionRepository.save(transaction);
        listing = listingRepository.save(listing);
        
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(Listing.ListingStatus.SOLD, listing.getStatus());
    }
    
    @Test
    void testOneTransactionPerListing() {
        Transaction transaction1 = new Transaction();
        transaction1.setListing(listing);
        transaction1.setBuyer(buyer);
        transaction1.setFinalPrice(new BigDecimal("99.99"));
        transaction1.setStatus(Transaction.TransactionStatus.PENDING);
        transactionRepository.save(transaction1);
        
        // Try to create second transaction for same listing - should fail
        assertThrows(Exception.class, () -> {
            Transaction transaction2 = new Transaction();
            transaction2.setListing(listing);
            transaction2.setBuyer(buyer);
            transaction2.setFinalPrice(new BigDecimal("99.99"));
            transaction2.setStatus(Transaction.TransactionStatus.PENDING);
            transactionRepository.save(transaction2);
            transactionRepository.flush(); // Force constraint check
        });
    }
}

