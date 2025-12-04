package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.ListingService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ListingControllerTest {
    
    @Mock
    private ListingService listingService;
    
    @InjectMocks
    private ListingController listingController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Listing testListing;
    private User testSeller;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();
        objectMapper = new ObjectMapper();
        
        testSeller = new User();
        testSeller.setId("seller-123");
        testSeller.setName("Seller");
        
        testCategory = new Category();
        testCategory.setId("category-123");
        testCategory.setName("Electronics");
        
        testListing = new Listing();
        testListing.setId("listing-123");
        testListing.setTitle("Test Item");
        testListing.setDescription("Test Description");
        testListing.setPrice(new BigDecimal("99.99"));
        testListing.setSeller(testSeller);
        testListing.setCategory(testCategory);
        testListing.setStatus(Listing.ListingStatus.ACTIVE);
        testListing.setCondition(Listing.ItemCondition.GOOD);
    }
    
    @Test
    void testGetListingById_Found() throws Exception {
        when(listingService.getListingById("listing-123")).thenReturn(Optional.of(testListing));
        
        mockMvc.perform(get("/api/listings/listing-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("listing-123"))
            .andExpect(jsonPath("$.title").value("Test Item"));
        
        verify(listingService, times(1)).getListingById("listing-123");
    }
    
    @Test
    void testGetListingById_NotFound() throws Exception {
        when(listingService.getListingById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/listings/non-existent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetAllListings() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getAllListings()).thenReturn(listings);
        
        mockMvc.perform(get("/api/listings")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test Item"));
    }
    
    @Test
    void testGetListingsBySeller() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getListingsBySeller("seller-123")).thenReturn(listings);
        
        mockMvc.perform(get("/api/listings/seller/seller-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("listing-123"));
    }
    
    @Test
    void testGetListingsByCategory() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getListingsByCategory("category-123")).thenReturn(listings);
        
        mockMvc.perform(get("/api/listings/category/category-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("listing-123"));
    }
    
    @Test
    void testGetListingsByStatus() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getListingsByStatus(Listing.ListingStatus.ACTIVE)).thenReturn(listings);
        
        mockMvc.perform(get("/api/listings/status/ACTIVE")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
    
    @Test
    void testGetListingsByCondition() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getListingsByCondition(Listing.ItemCondition.GOOD)).thenReturn(listings);
        
        mockMvc.perform(get("/api/listings/condition/GOOD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].condition").value("GOOD"));
    }
    
    @Test
    void testSearchListingsByKeyword() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.searchListings("Test")).thenReturn(listings);

        mockMvc.perform(get("/api/listings/search?searchTerm=Test")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test Item"));
    }
    
    @Test
    void testUpdateListing() throws Exception {
        testListing.setTitle("Updated Title");
        when(listingService.getListingById("listing-123")).thenReturn(Optional.of(testListing));
        when(listingService.updateListing(any(Listing.class))).thenReturn(testListing);
        
        mockMvc.perform(put("/api/listings/listing-123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testListing)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"));
    }
    
    @Test
    void testDeleteListing() throws Exception {
        mockMvc.perform(delete("/api/listings/listing-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(listingService, times(1)).deleteListing("listing-123");
    }
}
