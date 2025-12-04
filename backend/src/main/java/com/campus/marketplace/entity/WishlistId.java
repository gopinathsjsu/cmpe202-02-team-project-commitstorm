package com.campus.marketplace.entity;

import java.io.Serializable;
import java.util.Objects;

//Composite key for Wishlist entity.
public class WishlistId implements Serializable {
    
    private String userId;
    private String listingId;
    
    // Constructors
    public WishlistId() {}
    
    public WishlistId(String userId, String listingId) {
        this.userId = userId;
        this.listingId = listingId;
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
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistId that = (WishlistId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(listingId, that.listingId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, listingId);
    }
    
    @Override
    public String toString() {
        return "WishlistId{" +
                "userId='" + userId + '\'' +
                ", listingId='" + listingId + '\'' +
                '}';
    }
}