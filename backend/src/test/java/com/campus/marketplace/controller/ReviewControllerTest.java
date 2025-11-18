package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Review;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {
    
    @Mock
    private ReviewService reviewService;
    
    @InjectMocks
    private ReviewController reviewController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Review testReview;
    private User reviewer;
    private User seller;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
        
        reviewer = new User();
        reviewer.setId("reviewer-123");
        reviewer.setName("Reviewer");
        
        seller = new User();
        seller.setId("seller-123");
        seller.setName("Seller");
        
        Transaction transaction = new Transaction();
        transaction.setId("transaction-123");
        
        testReview = new Review();
        testReview.setId("review-123");
        testReview.setTransaction(transaction);
        testReview.setReviewer(reviewer);
        testReview.setSeller(seller);
        testReview.setRating(5);
        testReview.setComment("Great seller!");
    }
    
    @Test
    void testCreateReview() throws Exception {
        when(reviewService.createReview(any(), any(), any(), any(), any()))
            .thenReturn(testReview);
        
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReview)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));
    }
    
    @Test
    void testGetReviewById_Found() throws Exception {
        when(reviewService.getReviewById("review-123")).thenReturn(Optional.of(testReview));
        
        mockMvc.perform(get("/api/reviews/review-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("review-123"))
                .andExpect(jsonPath("$.rating").value(5));
    }
    
    @Test
    void testGetReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/reviews/non-existent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetAllReviews() throws Exception {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewService.getAllReviews()).thenReturn(reviews);
        
        mockMvc.perform(get("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5));
    }
    
    @Test
    void testGetReviewsBySeller() throws Exception {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewService.getReviewsBySeller("seller-123")).thenReturn(reviews);
        
        mockMvc.perform(get("/api/reviews/seller/seller-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("review-123"));
    }
    
    @Test
    void testGetAverageRating() throws Exception {
        when(reviewService.getAverageRatingBySeller("seller-123")).thenReturn(4.5);
        
        mockMvc.perform(get("/api/reviews/seller/seller-123/average-rating")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.5));
    }
    
    @Test
    void testUpdateReview() throws Exception {
        testReview.setRating(4);
        when(reviewService.getReviewById("review-123")).thenReturn(Optional.of(testReview));
        when(reviewService.updateReview(any(Review.class))).thenReturn(testReview);
        
        mockMvc.perform(put("/api/reviews/review-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));
    }
    
    @Test
    void testDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/reviews/review-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(reviewService, times(1)).deleteReview("review-123");
    }
}
