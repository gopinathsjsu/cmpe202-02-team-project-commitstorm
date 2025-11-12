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
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for ReviewController endpoints.
 */
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageService messageService;
    
    private User seller;
    private User buyer;
    private Category category;
    private Listing listing;
    private Transaction transaction;
    
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
        
        // Create buyer
        buyer = new User();
        buyer.setName("Test Buyer");
        buyer.setEmail("buyer@test.com");
        buyer.setPassword(passwordEncoder.encode("password123"));
        buyer.setRole(User.UserRole.USER);
        buyer.setStatus(User.UserStatus.ACTIVE);
        buyer = userRepository.save(buyer);
        
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
        listing.setStatus(Listing.ListingStatus.SOLD);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing = listingRepository.save(listing);
        
        // Create completed transaction
        transaction = new Transaction();
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(listing.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction = transactionRepository.save(transaction);
    }
    
    @Test
    void testCreateReview_Success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new ReviewDTO(
            transaction.getId(), buyer.getId(), seller.getId(), 5, "Great seller!"
        ));
        
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great seller!"));
        
        // Verify message was sent to seller
        var messages = messageService.getMessagesReceivedByUser(seller.getId());
        assertTrue(messages.stream().anyMatch(m -> m.getContent().contains("review")));
    }
    
    @Test
    void testCreateReview_InvalidTransaction() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new ReviewDTO(
            "invalid-transaction-id", buyer.getId(), seller.getId(), 5, "Great seller!"
        ));
        
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetReviewById() throws Exception {
        // First create a review
        String createBody = objectMapper.writeValueAsString(new ReviewDTO(
            transaction.getId(), buyer.getId(), seller.getId(), 5, "Great seller!"
        ));
        
        String response = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String reviewId = objectMapper.readTree(response).get("id").asText();
        
        // Get the review
        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.rating").value(5));
    }
    
    @Test
    void testGetReviewsBySeller() throws Exception {
        // Create a review
        String createBody = objectMapper.writeValueAsString(new ReviewDTO(
            transaction.getId(), buyer.getId(), seller.getId(), 5, "Great seller!"
        ));
        
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody));
        
        // Get reviews by seller
        mockMvc.perform(get("/api/reviews/seller/{sellerId}", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sellerId").value(seller.getId()));
    }
    
    // Helper class for request DTO
    @SuppressWarnings("unused")
    private static class ReviewDTO {
        public String transactionId;
        public String reviewerId;
        public String sellerId;
        public Integer rating;
        public String comment;
        
        public ReviewDTO(String transactionId, String reviewerId, String sellerId, Integer rating, String comment) {
            this.transactionId = transactionId;
            this.reviewerId = reviewerId;
            this.sellerId = sellerId;
            this.rating = rating;
            this.comment = comment;
        }
    }
}

