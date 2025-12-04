package com.campus.marketplace.service;

import com.campus.marketplace.entity.Review;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    
    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private MessageService messageService;
    
    @InjectMocks
    private ReviewService reviewService;
    
    private Review testReview;
    private User reviewer;
    private User seller;
    private Transaction transaction;
    private Listing testListing;
    
    @BeforeEach
    void setUp() {
        reviewer = new User();
        reviewer.setId("reviewer-123");
        reviewer.setName("Reviewer");
        reviewer.setEmail("reviewer@example.com");
        
        seller = new User();
        seller.setId("seller-123");
        seller.setName("Seller");
        seller.setEmail("seller@example.com");
        
        testListing = new Listing();
        testListing.setId("listing-123");
        testListing.setTitle("Test Item");
        testListing.setSeller(seller);
        
        transaction = new Transaction();
        transaction.setId("transaction-123");
        transaction.setListing(testListing);
        
        testReview = new Review();
        testReview.setId("review-123");
        testReview.setTransaction(transaction);
        testReview.setReviewer(reviewer);
        testReview.setSeller(seller);
        testReview.setRating(5);
        testReview.setComment("Great seller!");
    }
    
    @Test
    void testCreateReview_Success() {
        // Set transaction to COMPLETED status with buyer set to reviewer
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setBuyer(reviewer);
        
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(userRepository.findById("reviewer-123")).thenReturn(Optional.of(reviewer));
        when(userRepository.findById("seller-123")).thenReturn(Optional.of(seller));
        when(transactionRepository.findSellerIdByTransactionId("transaction-123")).thenReturn("seller-123");
        when(reviewRepository.findByTransactionId("transaction-123")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewRepository.findByIdWithDetails("review-123")).thenReturn(Optional.of(testReview));
        
        Review result = reviewService.createReview("transaction-123", "reviewer-123", "seller-123", 5, "Great!");
        
        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(messageService, times(1)).createSystemMessage(any(Listing.class), any(User.class), any(User.class), anyString());
    }
    
    @Test
    void testCreateReview_InvalidRating() {
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(userRepository.findById("reviewer-123")).thenReturn(Optional.of(reviewer));
        when(userRepository.findById("seller-123")).thenReturn(Optional.of(seller));
        
        assertThrows(RuntimeException.class, () -> 
            reviewService.createReview("transaction-123", "reviewer-123", "seller-123", 6, "Bad!")
        );
        
        verify(reviewRepository, never()).save(any(Review.class));
    }
    
    @Test
    void testGetReviewById_Found() {
        when(reviewRepository.findById("review-123")).thenReturn(Optional.of(testReview));
        
        Optional<Review> result = reviewService.getReviewById("review-123");
        
        assertTrue(result.isPresent());
        assertEquals("review-123", result.get().getId());
    }
    
    @Test
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        Optional<Review> result = reviewService.getReviewById("non-existent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetAllReviews_Empty() {
        List<Review> reviews = new ArrayList<>();
        when(reviewRepository.findAllWithDetails()).thenReturn(reviews);
        
        List<Review> result = reviewService.getAllReviews();
        
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(reviewRepository, times(1)).findAllWithDetails();
    }
    
    @Test
    void testGetReviewsBySeller() {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findBySellerIdOrderByCreatedAtDesc("seller-123")).thenReturn(reviews);
        
        List<Review> result = reviewService.getReviewsBySeller("seller-123");
        
        assertEquals(1, result.size());
        verify(reviewRepository, times(1)).findBySellerIdOrderByCreatedAtDesc("seller-123");
    }
    
    @Test
    void testGetReviewsByReviewer() {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByReviewerIdOrderByCreatedAtDesc("reviewer-123")).thenReturn(reviews);
        
        List<Review> result = reviewService.getReviewsByReviewer("reviewer-123");
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetAverageRating() {
        when(reviewRepository.findAverageRatingBySellerId("seller-123")).thenReturn(4.5);

        double result = reviewService.getAverageRatingBySeller("seller-123");
        
        assertEquals(4.5, result);
    }
    
    @Test
    void testUpdateReview() {
        testReview.setRating(4);
        when(reviewRepository.save(testReview)).thenReturn(testReview);
        
        Review result = reviewService.updateReview(testReview);
        
        assertEquals(4, result.getRating());
        verify(reviewRepository, times(1)).save(testReview);
    }
    
    @Test
    void testDeleteReview() {
        reviewService.deleteReview("review-123");
        
        verify(reviewRepository, times(1)).deleteById("review-123");
    }
}
