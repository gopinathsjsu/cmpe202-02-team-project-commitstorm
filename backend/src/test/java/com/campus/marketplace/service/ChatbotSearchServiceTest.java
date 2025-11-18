package com.campus.marketplace.service;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatbotSearchServiceTest {
    
    @Mock
    private ListingRepository listingRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ChatbotSearchService chatbotSearchService;
    
    private Listing activeListing;
    private Listing soldListing;
    private Category category;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatbotSearchService, "apiKey", "");
        ReflectionTestUtils.setField(chatbotSearchService, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(chatbotSearchService, "temperature", 0.3);
        
        category = new Category();
        category.setId("cat-123");
        category.setName("Books");
        
        activeListing = new Listing();
        activeListing.setId("listing-1");
        activeListing.setTitle("CMPE 202 Textbook");
        activeListing.setDescription("Textbook for CMPE 202 course");
        activeListing.setStatus(Listing.ListingStatus.ACTIVE);
        
        soldListing = new Listing();
        soldListing.setId("listing-2");
        soldListing.setTitle("CMPE 202 Textbook");
        soldListing.setDescription("Textbook for CMPE 202 course");
        soldListing.setStatus(Listing.ListingStatus.SOLD);
    }
    
    @Test
    void testProcessQuery_EmptyQuery() {
        // Act
        ChatbotSearchService.ChatbotSearchResult result = chatbotSearchService.processQuery("");
        
        // Assert
        assertNotNull(result);
        assertEquals("", result.getInterpretedQuery());
        assertTrue(result.getListings().isEmpty());
        assertTrue(result.isUsedFallback());
    }
    
    @Test
    void testProcessQuery_NullQuery() {
        // Act
        ChatbotSearchService.ChatbotSearchResult result = chatbotSearchService.processQuery(null);
        
        // Assert
        assertNotNull(result);
        assertEquals("", result.getInterpretedQuery());
        assertTrue(result.getListings().isEmpty());
        assertTrue(result.isUsedFallback());
    }
    
    @Test
    void testProcessQuery_FallbackKeywordExtraction() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(listingRepository.findByTitleOrDescriptionContaining("textbook", "textbook"))
                .thenReturn(Arrays.asList(activeListing));
        
        // Act
        ChatbotSearchService.ChatbotSearchResult result = 
            chatbotSearchService.processQuery("Do you have a textbook for CMPE 202?");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isUsedFallback());
        assertFalse(result.getInterpretedQuery().isEmpty());
        assertEquals(1, result.getListings().size());
        assertEquals("listing-1", result.getListings().get(0).getId());
        verify(listingRepository).findByTitleOrDescriptionContaining(anyString(), anyString());
    }
    
    @Test
    void testProcessQuery_FiltersInactiveListings() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(listingRepository.findByTitleOrDescriptionContaining("textbook", "textbook"))
                .thenReturn(Arrays.asList(activeListing, soldListing));
        
        // Act
        ChatbotSearchService.ChatbotSearchResult result = 
            chatbotSearchService.processQuery("textbook");
        
        // Assert
        assertEquals(1, result.getListings().size());
        assertEquals("listing-1", result.getListings().get(0).getId());
        assertTrue(result.getListings().stream()
            .allMatch(l -> l.getStatus() == Listing.ListingStatus.ACTIVE));
    }
    
    @Test
    void testProcessQuery_NoResults() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(listingRepository.findByTitleOrDescriptionContaining(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        
        // Act
        ChatbotSearchService.ChatbotSearchResult result = 
            chatbotSearchService.processQuery("nonexistent item");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getListings().isEmpty());
    }
    
    @Test
    void testProcessQuery_WithApiKeyButChatGPTFails() {
        // Arrange
        ReflectionTestUtils.setField(chatbotSearchService, "apiKey", "valid-api-key");
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(category));
        when(listingRepository.findByTitleOrDescriptionContaining(anyString(), anyString()))
                .thenReturn(Arrays.asList(activeListing));
        
        // Act - ChatGPT will fail (no actual API call in unit test), should fallback
        ChatbotSearchService.ChatbotSearchResult result = 
            chatbotSearchService.processQuery("textbook for CMPE 202");
        
        // Assert - Should use fallback
        assertNotNull(result);
        assertTrue(result.isUsedFallback());
        verify(listingRepository).findByTitleOrDescriptionContaining(anyString(), anyString());
    }
    
    @Test
    void testProcessQuery_ExtractCourseCode() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(listingRepository.findByTitleOrDescriptionContaining(anyString(), anyString()))
                .thenReturn(Arrays.asList(activeListing));
        
        // Act
        ChatbotSearchService.ChatbotSearchResult result = 
            chatbotSearchService.processQuery("textbook for CMPE 202");
        
        // Assert
        assertNotNull(result);
        // The fallback should extract "CMPE 202" from the query
        assertFalse(result.getInterpretedQuery().isEmpty());
    }
    
    // Note: Testing actual ChatGPT API calls would require:
    // 1. Mocking the OpenAiService
    // 2. Setting up test API keys (not recommended in unit tests)
    // 3. Integration tests for full ChatGPT interaction
    // The above tests cover the fallback behavior and result filtering logic.
}