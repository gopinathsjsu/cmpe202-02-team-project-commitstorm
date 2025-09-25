package com.campus.marketplace.controller;

import com.campus.marketplace.dto.ListingDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.entity.Category;
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
    
    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListingById(@PathVariable String id) {
        return listingService.getListingById(id)
                .map(listing -> ResponseEntity.ok(new ListingDTO(listing)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<ListingDTO>> getAllListings() {
        List<ListingDTO> listings = listingService.getAllListings().stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ListingDTO>> getListingsBySeller(@PathVariable String sellerId) {
        List<ListingDTO> listings = listingService.getListingsBySeller(sellerId).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ListingDTO>> getListingsByCategory(@PathVariable String categoryId) {
        List<ListingDTO> listings = listingService.getListingsByCategory(categoryId).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ListingDTO>> getListingsByStatus(@PathVariable Listing.ListingStatus status) {
        List<ListingDTO> listings = listingService.getListingsByStatus(status).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/condition/{condition}")
    public ResponseEntity<List<ListingDTO>> getListingsByCondition(@PathVariable Listing.ItemCondition condition) {
        List<ListingDTO> listings = listingService.getListingsByCondition(condition).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<ListingDTO>> getListingsByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        List<ListingDTO> listings = listingService.getListingsByPriceRange(minPrice, maxPrice).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ListingDTO>> searchListings(@RequestParam String searchTerm) {
        List<ListingDTO> listings = listingService.searchListings(searchTerm).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
    @GetMapping("/seller/{sellerId}/status/{status}")
    public ResponseEntity<List<ListingDTO>> getListingsBySellerAndStatus(
            @PathVariable String sellerId, 
            @PathVariable Listing.ListingStatus status) {
        List<ListingDTO> listings = listingService.getListingsBySellerAndStatus(sellerId, status).stream()
                .map(ListingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listings);
    }
    
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
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ListingDTO> updateListingStatus(@PathVariable String id, @RequestParam Listing.ListingStatus status) {
        try {
            Listing updatedListing = listingService.updateListingStatus(id, status);
            return ResponseEntity.ok(new ListingDTO(updatedListing));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable String id) {
        listingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }
}
