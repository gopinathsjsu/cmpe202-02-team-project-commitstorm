package com.campus.marketplace.service;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Category;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {
    
    @Mock
    private ListingRepository listingRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ListingService listingService;
    
    private Listing testListing;
    private User testSeller;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setId("seller-123");
        testSeller.setName("Test Seller");
        testSeller.setEmail("seller@example.com");
        
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
    void testCreateListing_WithoutId_GeneratesUUID() {
        Listing listing = new Listing();
        listing.setTitle("New Item");
        listing.setDescription("New Description");
        listing.setPrice(new BigDecimal("49.99"));
        listing.setSeller(testSeller);
        
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> {
            Listing savedListing = invocation.getArgument(0);
            assertNotNull(savedListing.getId());
            return savedListing;
        });
        
        Listing result = listingService.createListing(listing);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }
    
    @Test
    void testCreateListing_WithId_DoesNotOverrideId() {
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);
        
        Listing result = listingService.createListing(testListing);
        
        assertEquals("listing-123", result.getId());
        verify(listingRepository, times(1)).save(testListing);
    }
    
    @Test
    void testGetListingById_Found() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(testListing));
        
        Optional<Listing> result = listingService.getListingById("listing-123");
        
        assertTrue(result.isPresent());
        assertEquals("listing-123", result.get().getId());
        verify(listingRepository, times(1)).findById("listing-123");
    }
    
    @Test
    void testGetListingById_NotFound() {
        when(listingRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        Optional<Listing> result = listingService.getListingById("non-existent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetAllListings() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findAll()).thenReturn(listings);
        
        List<Listing> result = listingService.getAllListings();
        
        assertEquals(1, result.size());
        verify(listingRepository, times(1)).findAll();
    }
    
    @Test
    void testGetListingsBySeller() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findBySellerId("seller-123")).thenReturn(listings);
        
        List<Listing> result = listingService.getListingsBySeller("seller-123");
        
        assertEquals(1, result.size());
        assertEquals("seller-123", result.get(0).getSeller().getId());
        verify(listingRepository, times(1)).findBySellerId("seller-123");
    }
    
    @Test
    void testGetListingsByCategory() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findByCategoryId("category-123")).thenReturn(listings);
        
        List<Listing> result = listingService.getListingsByCategory("category-123");
        
        assertEquals(1, result.size());
        verify(listingRepository, times(1)).findByCategoryId("category-123");
    }
    
    @Test
    void testGetListingsByStatus() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findByStatus(Listing.ListingStatus.ACTIVE)).thenReturn(listings);
        
        List<Listing> result = listingService.getListingsByStatus(Listing.ListingStatus.ACTIVE);
        
        assertEquals(1, result.size());
        assertEquals(Listing.ListingStatus.ACTIVE, result.get(0).getStatus());
    }
    
    @Test
    void testGetListingsByCondition() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findByCondition(Listing.ItemCondition.GOOD)).thenReturn(listings);
        
        List<Listing> result = listingService.getListingsByCondition(Listing.ItemCondition.GOOD);
        
        assertEquals(1, result.size());
        assertEquals(Listing.ItemCondition.GOOD, result.get(0).getCondition());
    }
    
    @Test
    void testGetListingsByPriceRange() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findByPriceRange(
            new BigDecimal("50"), new BigDecimal("150")
        )).thenReturn(listings);
        
        List<Listing> result = listingService.getListingsByPriceRange(
            new BigDecimal("50"), new BigDecimal("150")
        );
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testSearchListingsByKeyword() {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingRepository.findByTitleOrDescriptionContaining("Test", "Test"))
            .thenReturn(listings);

        List<Listing> result = listingService.searchListings("Test");
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testUpdateListing() {
        testListing.setTitle("Updated Title");
        when(listingRepository.save(testListing)).thenReturn(testListing);
        
        Listing result = listingService.updateListing(testListing);
        
        assertEquals("Updated Title", result.getTitle());
        verify(listingRepository, times(1)).save(testListing);
    }
    
    @Test
    void testUpdateListingStatus() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(testListing);
        
        Listing result = listingService.updateListingStatus("listing-123", Listing.ListingStatus.SOLD);
        
        assertNotNull(result);
        verify(listingRepository, times(1)).findById("listing-123");
    }
    
    @Test
    void testDeleteListing() {
        listingService.deleteListing("listing-123");
        
        verify(listingRepository, times(1)).deleteById("listing-123");
    }
}
