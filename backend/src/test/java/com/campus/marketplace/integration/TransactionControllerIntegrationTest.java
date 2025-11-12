package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController endpoints.
 */
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private MessageService messageService;
    
    private User seller;
    private User buyer;
    private Category category;
    private Listing listing;
    private String sellerToken;
    private String buyerToken;
    
    @BeforeEach
    void setUp() {
        // Create seller
        seller = new User();
        seller.setName("Test Seller");
        seller.setEmail("seller@test.com");
        seller.setPassword(passwordEncoder.encode("password123"));
        seller.setRole(User.UserRole.USER);
        seller.setStatus(User.UserStatus.ACTIVE);
        seller = userRepository.save(seller);
        sellerToken = jwtUtil.generateToken(seller.getEmail());
        
        // Create buyer
        buyer = new User();
        buyer.setName("Test Buyer");
        buyer.setEmail("buyer@test.com");
        buyer.setPassword(passwordEncoder.encode("password123"));
        buyer.setRole(User.UserRole.USER);
        buyer.setStatus(User.UserStatus.ACTIVE);
        buyer = userRepository.save(buyer);
        buyerToken = jwtUtil.generateToken(buyer.getEmail());
        
        // Create category
        category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);
        
        // Create listing
        listing = new Listing();
        listing.setTitle("Test Item");
        listing.setDescription("Test Description");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing = listingRepository.save(listing);
    }
    
    @Test
    void testRequestToBuy_Success() throws Exception {
        mockMvc.perform(post("/api/transactions/request-to-buy")
                .param("listingId", listing.getId())
                .param("buyerId", buyer.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listingId").value(listing.getId()))
                .andExpect(jsonPath("$.buyerId").value(buyer.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        // Verify listing status changed to PENDING
        Listing updatedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertEquals(Listing.ListingStatus.PENDING, updatedListing.getStatus());
        
        // Verify message was sent to seller
        var messages = messageService.getMessagesReceivedByUser(seller.getId());
        assertTrue(messages.size() > 0);
    }
    
    @Test
    void testRequestToBuy_OwnListing() throws Exception {
        mockMvc.perform(post("/api/transactions/request-to-buy")
                .param("listingId", listing.getId())
                .param("buyerId", seller.getId())) // Seller trying to buy own listing
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRequestToBuy_ListingNotActive() throws Exception {
        listing.setStatus(Listing.ListingStatus.SOLD);
        listingRepository.save(listing);
        
        mockMvc.perform(post("/api/transactions/request-to-buy")
                .param("listingId", listing.getId())
                .param("buyerId", buyer.getId()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testMarkAsSold_Success() throws Exception {
        // First create a transaction
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(patch("/api/transactions/{transactionId}/mark-sold", transaction.getId())
                .param("sellerId", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        // Verify listing status changed to SOLD
        Listing updatedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertEquals(Listing.ListingStatus.SOLD, updatedListing.getStatus());
        
        // Verify message was sent to buyer
        var messages = messageService.getMessagesReceivedByUser(buyer.getId());
        assertTrue(messages.stream().anyMatch(m -> m.getContent().contains("accepted")));
    }
    
    @Test
    void testMarkAsSold_WrongSeller() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        // Try with wrong seller
        User otherSeller = new User();
        otherSeller.setName("Other Seller");
        otherSeller.setEmail("other@test.com");
        otherSeller.setPassword(passwordEncoder.encode("password123"));
        otherSeller.setRole(User.UserRole.USER);
        otherSeller.setStatus(User.UserStatus.ACTIVE);
        otherSeller = userRepository.save(otherSeller);
        
        mockMvc.perform(patch("/api/transactions/{transactionId}/mark-sold", transaction.getId())
                .param("sellerId", otherSeller.getId()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRejectRequest_Success() throws Exception {
        // First create a transaction
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(patch("/api/transactions/{transactionId}/reject", transaction.getId())
                .param("sellerId", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        // Verify listing status changed back to ACTIVE
        Listing updatedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertEquals(Listing.ListingStatus.ACTIVE, updatedListing.getStatus());
        
        // Verify message was sent to buyer
        var messages = messageService.getMessagesReceivedByUser(buyer.getId());
        assertTrue(messages.stream().anyMatch(m -> m.getContent().contains("not to proceed")));
    }
    
    @Test
    void testGetTransactionById() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(get("/api/transactions/{id}", transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId()))
                .andExpect(jsonPath("$.listingId").value(listing.getId()));
    }
    
    @Test
    void testGetTransactionByListingId() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(get("/api/transactions/listing/{listingId}", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId").value(listing.getId()));
    }
    
    @Test
    void testGetTransactionsByBuyer() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(get("/api/transactions/buyer/{buyerId}", buyer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerId").value(buyer.getId()));
    }
    
    @Test
    void testGetTransactionsBySeller() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        transaction = transactionRepository.save(transaction);
        
        mockMvc.perform(get("/api/transactions/seller/{sellerId}", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

