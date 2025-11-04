package com.campus.marketplace.controller;

import com.campus.marketplace.dto.ChatbotSearchRequest;
import com.campus.marketplace.dto.ChatbotSearchResponse;
import com.campus.marketplace.dto.ListingDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Category;
import com.campus.marketplace.service.ChatbotSearchService;
import com.campus.marketplace.service.ListingService;
import com.campus.marketplace.service.UserService;
import com.campus.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listing endpoints: CRUD, filters, search, and pagination.
 */
@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {
    
    @Autowired
    private ListingService listingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ChatbotSearchService chatbotSearchService;
    
    /**
     * Create a listing.
     * @param listingDTO sellerId, title, description, price, categoryId, condition, images, status
     * @return 201 with ListingDTO
     */
    @PostMapping
    public ResponseEntity<ListingDTO> createListing(@Valid @RequestBody ListingDTO listingDTO) {
        User seller = userService.getUserById(listingDTO.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Category category = categoryService.getCategoryById(listingDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setTitle(listingDTO.getTitle());
        listing.setDescription(listingDTO.getDescription());
        listing.setPrice(listingDTO.getPrice());
        listing.setCategory(category);
        listing.setCondition(listingDTO.getCondition());
        listing.setImages(listingDTO.getImages());
        listing.setStatus(listingDTO.getStatus() != null ? listingDTO.getStatus() : Listing.ListingStatus.ACTIVE);
        
        Listing createdListing = listingService.createListing(listing);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ListingDTO(createdListing));
    }
    
    /**
     * Get listing by id.
     * @param id listing id
     * @return 200 with ListingDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListingById(@PathVariable String id) {
        return listingService.getListingById(id)
                .map(listing -> ResponseEntity.ok(new ListingDTO(listing)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * List all listings.
     * @return 200 with list of ListingDTO
     */
    @GetMapping
    public ResponseEntity<List<ListingDTO>> getAllListings() {
        List<ListingDTO> listings = listingService.getAllListings().stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * List listings by seller.
     * @param sellerId seller id
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ListingDTO>> getListingsBySeller(@PathVariable String sellerId) {
        List<ListingDTO> listings = listingService.getListingsBySeller(sellerId).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * List listings by category.
     * @param categoryId category id
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ListingDTO>> getListingsByCategory(@PathVariable String categoryId) {
        List<ListingDTO> listings = listingService.getListingsByCategory(categoryId).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * List listings by status.
     * @param status listing status
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ListingDTO>> getListingsByStatus(@PathVariable Listing.ListingStatus status) {
        List<ListingDTO> listings = listingService.getListingsByStatus(status).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * List listings by item condition.
     * @param condition condition enum
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/condition/{condition}")
    public ResponseEntity<List<ListingDTO>> getListingsByCondition(@PathVariable Listing.ItemCondition condition) {
        List<ListingDTO> listings = listingService.getListingsByCondition(condition).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * List listings within price range.
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ListingDTO>> getListingsByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        List<ListingDTO> listings = listingService.getListingsByPriceRange(minPrice, maxPrice).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * Search listings by term (title/description).
     * @param searchTerm query text
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/search")
    public ResponseEntity<List<ListingDTO>> searchListings(@RequestParam String searchTerm) {
        List<ListingDTO> listings = listingService.searchListings(searchTerm).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * Chatbot search: process conversational queries (e.g., "textbook for CMPE 202?")
     * Uses ChatGPT API to interpret natural language and returns search results.
     * Falls back to keyword extraction if ChatGPT is unavailable.
     * 
     * @param request chatbot search request with query
     * @return 200 with ChatbotSearchResponse containing interpreted query and results
     */
    @PostMapping("/chatbot-search")
    public ResponseEntity<ChatbotSearchResponse> chatbotSearch(@Valid @RequestBody ChatbotSearchRequest request) {
        ChatbotSearchService.ChatbotSearchResult result = chatbotSearchService.processQuery(request.getQuery());
        
        List<ListingDTO> listingDTOs = result.getListings().stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        
        String message = listingDTOs.isEmpty() 
            ? "No listings found matching your query."
            : "Found " + listingDTOs.size() + " listing(s) matching your query.";
        
        if (result.isUsedFallback()) {
            message += " (Using keyword search fallback)";
        }
        
        ChatbotSearchResponse response = new ChatbotSearchResponse(
            result.getInterpretedQuery(),
            listingDTOs,
            message,
            result.isUsedFallback()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Chatbot search via GET: process conversational queries via query parameter.
     * Convenience endpoint for simpler client integration.
     * 
     * @param query natural language query
     * @return 200 with ChatbotSearchResponse containing interpreted query and results
     */
    @GetMapping("/chatbot-search")
    public ResponseEntity<ChatbotSearchResponse> chatbotSearchGet(@RequestParam String query) {
        ChatbotSearchService.ChatbotSearchResult result = chatbotSearchService.processQuery(query);
        
        List<ListingDTO> listingDTOs = result.getListings().stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        
        String message = listingDTOs.isEmpty() 
            ? "No listings found matching your query."
            : "Found " + listingDTOs.size() + " listing(s) matching your query.";
        
        if (result.isUsedFallback()) {
            message += " (Using keyword search fallback)";
        }
        
        ChatbotSearchResponse response = new ChatbotSearchResponse(
            result.getInterpretedQuery(),
            listingDTOs,
            message,
            result.isUsedFallback()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * List listings by seller and status.
     * @param sellerId seller id
     * @param status listing status
     * @return 200 with list of ListingDTO
     */
    @GetMapping("/seller/{sellerId}/status/{status}")
    public ResponseEntity<List<ListingDTO>> getListingsBySellerAndStatus(
            @PathVariable String sellerId, 
            @PathVariable Listing.ListingStatus status) {
        List<ListingDTO> listings = listingService.getListingsBySellerAndStatus(sellerId, status).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    /**
     * Page listings by category and status.
     * @param categoryId category id
     * @param status listing status
     * @param page page number
     * @param size page size
     * @return 200 with Page of ListingDTO
     */
    @GetMapping("/category/{categoryId}/status/{status}")
    public ResponseEntity<Page<ListingDTO>> getListingsByCategoryAndStatus(
            @PathVariable String categoryId, 
            @PathVariable Listing.ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingService.getListingsByCategoryAndStatus(categoryId, status, pageable);
        Page<ListingDTO> listingDTOs = listings.map(ListingDTO::new);
        return ResponseEntity.ok(listingDTOs);
    }
    
    /**
     * Page listings by status ordered by creation time desc.
     * @param status listing status
     * @param page page number
     * @param size page size
     * @return 200 with Page of ListingDTO
     */
    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<ListingDTO>> getListingsByStatusOrderByCreatedAtDesc(
            @PathVariable Listing.ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingService.getListingsByStatusOrderByCreatedAtDesc(status, pageable);
        Page<ListingDTO> listingDTOs = listings.map(ListingDTO::new);
        return ResponseEntity.ok(listingDTOs);
    }
    
    /**
     * Page listings by seller ordered by creation time desc.
     * @param sellerId seller id
     * @param page page number
     * @param size page size
     * @return 200 with Page of ListingDTO
     */
    @GetMapping("/seller/{sellerId}/page")
    public ResponseEntity<Page<ListingDTO>> getListingsBySellerOrderByCreatedAtDesc(
            @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingService.getListingsBySellerOrderByCreatedAtDesc(sellerId, pageable);
        Page<ListingDTO> listingDTOs = listings.map(ListingDTO::new);
        return ResponseEntity.ok(listingDTOs);
    }
    
    /**
     * Update listing fields.
     * @param id listing id
     * @param listingDTO editable fields
     * @return 200 with updated ListingDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ListingDTO> updateListing(@PathVariable String id, @Valid @RequestBody ListingDTO listingDTO) {
        return listingService.getListingById(id)
                .map(existingListing -> {
                    existingListing.setTitle(listingDTO.getTitle());
                    existingListing.setDescription(listingDTO.getDescription());
                    existingListing.setPrice(listingDTO.getPrice());
                    existingListing.setCondition(listingDTO.getCondition());
                    existingListing.setImages(listingDTO.getImages());
                    existingListing.setStatus(listingDTO.getStatus());
                    
                    if (listingDTO.getCategoryId() != null && !listingDTO.getCategoryId().equals(existingListing.getCategory().getId())) {
                        Category category = categoryService.getCategoryById(listingDTO.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));
                        existingListing.setCategory(category);
                    }
                    
                    Listing updatedListing = listingService.updateListing(existingListing);
                    return ResponseEntity.ok(new ListingDTO(updatedListing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update listing status only.
     * @param id listing id
     * @param status new status
     * @return 200 with updated ListingDTO or 404 if not found
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ListingDTO> updateListingStatus(@PathVariable String id, @RequestParam Listing.ListingStatus status) {
        try {
            Listing updatedListing = listingService.updateListingStatus(id, status);
            return ResponseEntity.ok(new ListingDTO(updatedListing));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete listing.
     * @param id listing id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable String id) {
        listingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }
}
