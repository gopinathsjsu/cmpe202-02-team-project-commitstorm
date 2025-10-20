package com.campus.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "wishlist")
@IdClass(WishlistId.class)
public class Wishlist {
    
    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;
    
    @Id
    @Column(name = "listing_id", columnDefinition = "CHAR(36)", nullable = false)
    private String listingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Wishlist() {}
    
    public Wishlist(String userId, String listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }
    
    public Wishlist(User user, Listing listing) {
        this.user = user;
        this.listing = listing;
        this.userId = user.getId();
        this.listingId = listing.getId();
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }
    
    public Listing getListing() {
        return listing;
    }
    
    public void setListing(Listing listing) {
        this.listing = listing;
        this.listingId = listing != null ? listing.getId() : null;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wishlist wishlist = (Wishlist) o;
        return Objects.equals(userId, wishlist.userId) && Objects.equals(listingId, wishlist.listingId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, listingId);
    }
    
    @Override
    public String toString() {
        return "Wishlist{" +
                "userId='" + userId + '\'' +
                ", listingId='" + listingId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}