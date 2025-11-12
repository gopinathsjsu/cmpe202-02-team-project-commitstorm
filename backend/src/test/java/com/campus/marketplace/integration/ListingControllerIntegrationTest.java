package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.util.JwtUtil;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ListingController endpoints.
 */
@AutoConfigureMockMvc
@Transactional
public class ListingControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
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
    private ObjectMapper objectMapper;
    
    private User seller;
    private Category category;
    private Listing listing;
    private String sellerToken;
    
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
    void testCreateListing() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new ListingDTO(
            "New Item", "New Description", new BigDecimal("149.99"), 
            category.getId(), "NEW"
        ));
        
        mockMvc.perform(post("/api/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Item"))
                .andExpect(jsonPath("$.price").value(149.99));
    }
    
    @Test
    void testGetListingById() throws Exception {
        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listing.getId()))
                .andExpect(jsonPath("$.title").value("Test Item"));
    }
    
    @Test
    void testGetAllListings() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testGetListingsBySeller() throws Exception {
        mockMvc.perform(get("/api/listings/seller/{sellerId}", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sellerId").value(seller.getId()));
    }
    
    @Test
    void testSearchWithFilters() throws Exception {
        mockMvc.perform(get("/api/listings/search/advanced")
                .param("keyword", "Test")
                .param("categoryId", category.getId())
                .param("minPrice", "0")
                .param("maxPrice", "200")
                .param("sortBy", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    void testUpdateListing() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new ListingDTO(
            "Updated Item", "Updated Description", new BigDecimal("199.99"), 
            category.getId(), "LIKE_NEW"
        ));
        
        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Item"));
    }
    
    @Test
    void testDeleteListing() throws Exception {
        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isNoContent());
    }
    
    // Helper class for request DTO
    @SuppressWarnings("unused")
    private static class ListingDTO {
        public String title;
        public String description;
        public BigDecimal price;
        public String categoryId;
        public String condition;
        
        public ListingDTO(String title, String description, BigDecimal price, 
                         String categoryId, String condition) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.categoryId = categoryId;
            this.condition = condition;
        }
    }
}

