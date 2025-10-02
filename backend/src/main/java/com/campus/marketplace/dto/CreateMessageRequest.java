package com.campus.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateMessageRequest {
    
    @NotNull(message = "Listing ID is required")
    @NotBlank(message = "Listing ID cannot be blank")
    private String listingId;
    
    @NotNull(message = "To user ID is required")
    @NotBlank(message = "To user ID cannot be blank")
    private String toUserId;
    
    @NotNull(message = "Content is required")
    @NotBlank(message = "Content cannot be blank")
    private String content;
    
    // Constructors
    public CreateMessageRequest() {}
    
    public CreateMessageRequest(String listingId, String toUserId, String content) {
        this.listingId = listingId;
        this.toUserId = toUserId;
        this.content = content;
    }
    
    // Getters and Setters
    public String getListingId() {
        return listingId;
    }
    
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
    
    public String getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
