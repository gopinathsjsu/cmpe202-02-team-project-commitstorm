package com.campus.marketplace.entity;

import java.io.Serializable;
import java.util.Objects;

public class WishlistId implements Serializable {
    
    private String user;
    private String listing;
    
    // Constructors
    public WishlistId() {}
    
    public WishlistId(String user, String listing) {
        this.user = user;
        this.listing = listing;
    }
    
    // Getters and Setters
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getListing() {
        return listing;
    }
    
    public void setListing(String listing) {
        this.listing = listing;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistId that = (WishlistId) o;
        return Objects.equals(user, that.user) && Objects.equals(listing, that.listing);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, listing);
    }
}
