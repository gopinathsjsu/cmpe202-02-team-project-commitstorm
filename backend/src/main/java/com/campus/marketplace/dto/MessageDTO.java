package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MessageDTO {
    
    private String id;
    private String listingId;
    private String listingTitle;
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    private String content;
    private Boolean isRead;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Constructors
    public MessageDTO() {}
    
    public MessageDTO(Message message) {
        this.id = message.getId();
        this.listingId = message.getListing().getId();
        this.listingTitle = message.getListing().getTitle();
        this.fromUserId = message.getFromUser().getId();
        this.fromUserName = message.getFromUser().getName();
        this.toUserId = message.getToUser().getId();
        this.toUserName = message.getToUser().getName();
        this.content = message.getContent();
        this.isRead = message.getIsRead();
        this.createdAt = message.getCreatedAt();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getListingId() {
        return listingId;
    }
    
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
    
    public String getListingTitle() {
        return listingTitle;
    }
    
    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }
    
    public String getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public String getFromUserName() {
        return fromUserName;
    }
    
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    
    public String getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }
    
    public String getToUserName() {
        return toUserName;
    }
    
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}