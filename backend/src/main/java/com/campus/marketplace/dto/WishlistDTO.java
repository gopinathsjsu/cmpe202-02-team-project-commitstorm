package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Wishlist;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

// DTO for Wishlist: used for requests/responses.
public class WishlistDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Listing ID is required")
    private String listingId;
    
    // Additional fields for response
    private String userName;
    private String listingTitle;
    private String listingDescription;
    private String listingPrice;
    private String listingImage;
    private String categoryName;
    private String sellerName;
    private LocalDateTime createdAt;
    
    // Constructors
    /** No-args constructor for serialization. */
    public WishlistDTO() {}
    
    /**
     * Map from Wishlist entity to DTO.
     * @param wishlist source entity
     */
    public WishlistDTO(Wishlist wishlist) {
        this.userId = wishlist.getUserId();
        this.listingId = wishlist.getListingId();
        this.createdAt = wishlist.getCreatedAt();
        
        // Populate additional fields if entities are loaded
        if (wishlist.getUser() != null) {
            this.userName = wishlist.getUser().getName();
        }
        
        if (wishlist.getListing() != null) {
            this.listingTitle = wishlist.getListing().getTitle();
            this.listingDescription = wishlist.getListing().getDescription();
            this.listingPrice = wishlist.getListing().getPrice() != null ? 
                wishlist.getListing().getPrice().toString() : null;
            this.listingImage = wishlist.getListing().getImages();
            this.sellerName = wishlist.getListing().getSeller() != null ? 
                wishlist.getListing().getSeller().getName() : null;
            
            if (wishlist.getListing().getCategory() != null) {
                this.categoryName = wishlist.getListing().getCategory().getName();
            }
        }
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getListingId() {
        return listingId;
    }
    
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getListingTitle() {
        return listingTitle;
    }
    
    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }
    
    public String getListingDescription() {
        return listingDescription;
    }
    
    public void setListingDescription(String listingDescription) {
        this.listingDescription = listingDescription;
    }
    
    public String getListingPrice() {
        return listingPrice;
    }
    
    public void setListingPrice(String listingPrice) {
        this.listingPrice = listingPrice;
    }
    
    public String getListingImage() {
        return listingImage;
    }
    
    public void setListingImage(String listingImage) {
        this.listingImage = listingImage;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}