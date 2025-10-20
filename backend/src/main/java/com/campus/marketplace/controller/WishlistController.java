package com.campus.marketplace.controller;

import com.campus.marketplace.dto.WishlistDTO;
import com.campus.marketplace.entity.Wishlist;
import com.campus.marketplace.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wishlist endpoints: add/remove items, get user's wishlist, check if item is wishlisted
 */
@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
@Tag(name = "Wishlist", description = "Wishlist API endpoints")
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    /**
     * Add a listing to user's wishlist
     * @param wishlistDTO user and listing IDs
     * @return 201 with WishlistDTO or 400 if already in wishlist
     */
    @PostMapping
    @Operation(summary = "Add listing to wishlist", description = "Add a listing to user's wishlist")
    public ResponseEntity<?> addToWishlist(@Valid @RequestBody WishlistDTO wishlistDTO) {
        try {
            Wishlist wishlist = wishlistService.addToWishlist(
                wishlistDTO.getUserId(), 
                wishlistDTO.getListingId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new WishlistDTO(wishlist));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Remove a listing from user's wishlist
     * @param userId user id
     * @param listingId listing id
     * @return 204 if successful, 400 if not in wishlist
     */
    @DeleteMapping("/{userId}/{listingId}")
    @Operation(summary = "Remove listing from wishlist", description = "Remove a listing from user's wishlist")
    public ResponseEntity<Void> removeFromWishlist(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Listing ID") @PathVariable String listingId) {
        try {
            wishlistService.removeFromWishlist(userId, listingId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's wishlist with details
     * @param userId user id
     * @return 200 with list of WishlistDTO
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's wishlist", description = "Get all items in user's wishlist with details")
    public ResponseEntity<?> getUserWishlist(
            @Parameter(description = "User ID") @PathVariable String userId) {
        try {
            List<Wishlist> wishlist = wishlistService.getUserWishlist(userId);
            List<WishlistDTO> wishlistDTOs = wishlist.stream()
                    .map(WishlistDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(wishlistDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Check if a listing is in user's wishlist
     * @param userId user id
     * @param listingId listing id
     * @return 200 with boolean result
     */
    @GetMapping("/{userId}/{listingId}")
    @Operation(summary = "Check if item is wishlisted", description = "Check if a listing is in user's wishlist")
    public ResponseEntity<Boolean> isInWishlist(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Listing ID") @PathVariable String listingId) {
        boolean isWishlisted = wishlistService.isInWishlist(userId, listingId);
        return ResponseEntity.ok(isWishlisted);
    }
    
    /**
     * Get count of items in user's wishlist
     * @param userId user id
     * @return 200 with count
     */
    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get wishlist count", description = "Get the number of items in user's wishlist")
    public ResponseEntity<Long> getWishlistCount(
            @Parameter(description = "User ID") @PathVariable String userId) {
        try {
            long count = wishlistService.getWishlistCount(userId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get count of users who have a listing in their wishlist
     * @param listingId listing id
     * @return 200 with count
     */
    @GetMapping("/listing/{listingId}/count")
    @Operation(summary = "Get wishlist count for listing", description = "Get the number of users who have this listing in their wishlist")
    public ResponseEntity<Long> getWishlistCountForListing(
            @Parameter(description = "Listing ID") @PathVariable String listingId) {
        try {
            long count = wishlistService.getWishlistCountForListing(listingId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all users who have a specific listing in their wishlist
     * @param listingId listing id
     * @return 200 with list of WishlistDTO
     */
    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get users who wishlisted listing", description = "Get all users who have this listing in their wishlist")
    public ResponseEntity<List<WishlistDTO>> getWishlistUsersForListing(
            @Parameter(description = "Listing ID") @PathVariable String listingId) {
        try {
            List<Wishlist> wishlist = wishlistService.getWishlistUsersForListing(listingId);
            List<WishlistDTO> wishlistDTOs = wishlist.stream()
                    .map(WishlistDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(wishlistDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Clear user's entire wishlist
     * @param userId user id
     * @return 204 if successful
     */
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear user's wishlist", description = "Remove all items from user's wishlist")
    public ResponseEntity<Void> clearUserWishlist(
            @Parameter(description = "User ID") @PathVariable String userId) {
        try {
            wishlistService.clearUserWishlist(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}