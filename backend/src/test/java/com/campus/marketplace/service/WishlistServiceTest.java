package com.campus.marketplace.service;

import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.WishlistRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {
    
    @Mock
    private WishlistRepository wishlistRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ListingRepository listingRepository;
    
    @InjectMocks
    private WishlistService wishlistService;
    
    private Wishlist testWishlist;
    private User testUser;
    private Listing testListing;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        
        testListing = new Listing();
        testListing.setId("listing-123");
        testListing.setTitle("Test Item");
        
        testWishlist = new Wishlist();
        testWishlist.setUserId("user-123");
        testWishlist.setListingId("listing-123");
    }
    
    @Test
    void testAddToWishlist_Success() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(testListing));
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);
        
        Wishlist result = wishlistService.addToWishlist("user-123", "listing-123");
        
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("listing-123", result.getListingId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }
    
    @Test
    void testAddToWishlist_UserNotFound() {
        when(userRepository.findById("user-999")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.addToWishlist("user-999", "listing-123")
        );
        
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }
    
    @Test
    void testAddToWishlist_ListingNotFound() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(listingRepository.findById("listing-999")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.addToWishlist("user-123", "listing-999")
        );
        
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }
    
    @Test
    void testAddToWishlist_AlreadyExists() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(testListing));
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.addToWishlist("user-123", "listing-123")
        );
        
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }
    
    @Test
    void testRemoveFromWishlist_Success() {
        when(userRepository.existsById("user-123")).thenReturn(true);
        when(listingRepository.existsById("listing-123")).thenReturn(true);
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(true);
        
        assertDoesNotThrow(() -> 
            wishlistService.removeFromWishlist("user-123", "listing-123")
        );
        
        verify(wishlistRepository, times(1)).deleteByUserIdAndListingId("user-123", "listing-123");
    }
    
    @Test
    void testRemoveFromWishlist_UserNotFound() {
        when(userRepository.existsById("user-999")).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.removeFromWishlist("user-999", "listing-123")
        );
    }
    
    @Test
    void testRemoveFromWishlist_ListingNotFound() {
        when(userRepository.existsById("user-123")).thenReturn(true);
        when(listingRepository.existsById("listing-999")).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.removeFromWishlist("user-123", "listing-999")
        );
    }
    
    @Test
    void testRemoveFromWishlist_NotInWishlist() {
        when(userRepository.existsById("user-123")).thenReturn(true);
        when(listingRepository.existsById("listing-123")).thenReturn(true);
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> 
            wishlistService.removeFromWishlist("user-123", "listing-123")
        );
    }
    
    @Test
    void testGetUserWishlist() {
        List<Wishlist> wishlists = Arrays.asList(testWishlist);
        when(userRepository.existsById("user-123")).thenReturn(true);
        when(wishlistRepository.findByUserIdWithDetails("user-123")).thenReturn(wishlists);

        List<Wishlist> result = wishlistService.getUserWishlist("user-123");
        
        assertEquals(1, result.size());
        assertEquals("user-123", result.get(0).getUserId());
        verify(wishlistRepository, times(1)).findByUserIdWithDetails("user-123");
    }
    
    @Test
    void testIsInWishlist_True() {
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(true);
        
        boolean result = wishlistService.isInWishlist("user-123", "listing-123");
        
        assertTrue(result);
    }
    
    @Test
    void testIsInWishlist_False() {
        when(wishlistRepository.existsByUserIdAndListingId("user-123", "listing-123"))
            .thenReturn(false);
        
        boolean result = wishlistService.isInWishlist("user-123", "listing-123");
        
        assertFalse(result);
    }
    
    @Test
    void testClearWishlist() {
        when(userRepository.existsById("user-123")).thenReturn(true);
        when(wishlistRepository.findByUserIdOrderByCreatedAtDesc("user-123")).thenReturn(Arrays.asList(testWishlist));

        wishlistService.clearUserWishlist("user-123");

        verify(wishlistRepository, times(1)).deleteAll(anyList());
    }
}
