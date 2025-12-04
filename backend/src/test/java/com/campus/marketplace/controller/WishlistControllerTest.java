package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.service.WishlistService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WishlistControllerTest {
    
    @Mock
    private WishlistService wishlistService;
    
    @InjectMocks
    private WishlistController wishlistController;
    
    private MockMvc mockMvc;
    private Wishlist testWishlist;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wishlistController).build();
        
        testWishlist = new Wishlist();
        testWishlist.setUserId("user-123");
        testWishlist.setListingId("listing-123");
    }
    
    @Test
    void testAddToWishlist() throws Exception {
        when(wishlistService.addToWishlist("user-123", "listing-123")).thenReturn(testWishlist);

        String body = "{\"userId\":\"user-123\",\"listingId\":\"listing-123\"}";

        mockMvc.perform(post("/api/wishlist")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("user-123"))
            .andExpect(jsonPath("$.listingId").value("listing-123"));

        verify(wishlistService, times(1)).addToWishlist("user-123", "listing-123");
    }
    
    @Test
    void testAddToWishlist_UserNotFound() throws Exception {
        when(wishlistService.addToWishlist("user-999", "listing-123"))
            .thenThrow(new RuntimeException("User not found with id: user-999"));
        
        String body = "{\"userId\":\"user-999\",\"listingId\":\"listing-123\"}";
        
        mockMvc.perform(post("/api/wishlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetUserWishlist() throws Exception {
        List<Wishlist> wishlists = Arrays.asList(testWishlist);
        when(wishlistService.getUserWishlist("user-123")).thenReturn(wishlists);
        
        mockMvc.perform(get("/api/wishlist/user/user-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value("user-123"));

        verify(wishlistService, times(1)).getUserWishlist("user-123");
    }
    
    @Test
    void testRemoveFromWishlist() throws Exception {
        mockMvc.perform(delete("/api/wishlist/user-123/listing-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(wishlistService, times(1)).removeFromWishlist("user-123", "listing-123");
    }
    
    @Test
    void testIsInWishlist() throws Exception {
        when(wishlistService.isInWishlist("user-123", "listing-123")).thenReturn(true);
        
        mockMvc.perform(get("/api/wishlist/user-123/listing-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
    
    @Test
    void testClearWishlist() throws Exception {
        mockMvc.perform(delete("/api/wishlist/user/user-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(wishlistService, times(1)).clearUserWishlist("user-123");
    }
}
