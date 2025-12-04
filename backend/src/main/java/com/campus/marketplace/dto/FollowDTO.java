package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Follow;

import java.time.LocalDateTime;

public class FollowDTO {
    
    private String id;
    private String followerId;
    private String followerName;
    private String sellerId;
    private String sellerName;
    private LocalDateTime createdAt;
    
    // Constructors
    public FollowDTO() {}
    
    public FollowDTO(Follow follow) {
        this.id = follow.getId();
        this.followerId = follow.getFollowerId();
        this.followerName = follow.getFollower() != null ? follow.getFollower().getName() : null;
        this.sellerId = follow.getSellerId();
        this.sellerName = follow.getSeller() != null ? follow.getSeller().getName() : null;
        this.createdAt = follow.getCreatedAt();
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
    
    public String getFollowerName() {
        return followerName;
    }
    
    public void setFollowerName(String followerName) {
        this.followerName = followerName;
    }
    
    public String getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
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

