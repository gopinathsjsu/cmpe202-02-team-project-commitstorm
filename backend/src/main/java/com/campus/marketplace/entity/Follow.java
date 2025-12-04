package com.campus.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "seller_id"}))
public class Follow {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "follower_id", columnDefinition = "CHAR(36)", nullable = false)
    private String followerId;
    
    @Column(name = "seller_id", columnDefinition = "CHAR(36)", nullable = false)
    private String sellerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Follow() {}
    
    public Follow(String followerId, String sellerId) {
        this.followerId = followerId;
        this.sellerId = sellerId;
    }
    
    public Follow(User follower, User seller) {
        this.follower = follower;
        this.seller = seller;
        this.followerId = follower != null ? follower.getId() : null;
        this.sellerId = seller != null ? seller.getId() : null;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFollowerId() {
        return followerId;
    }
    
    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
    
    public String getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
    
    public User getFollower() {
        return follower;
    }
    
    public void setFollower(User follower) {
        this.follower = follower;
        this.followerId = follower != null ? follower.getId() : null;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
        this.sellerId = seller != null ? seller.getId() : null;
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
        Follow follow = (Follow) o;
        return Objects.equals(followerId, follow.followerId) && Objects.equals(sellerId, follow.sellerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(followerId, sellerId);
    }
    
    @Override
    public String toString() {
        return "Follow{" +
                "followerId='" + followerId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

