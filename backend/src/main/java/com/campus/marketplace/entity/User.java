package com.campus.marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Listing> listings;
    
    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> purchases;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wishlist> wishlistItems;
    
    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> sentMessages;
    
    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> receivedMessages;
    
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;
    
    @OneToMany(mappedBy = "moderator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> moderatedReports;
    
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> receivedReviews;
    
    // Constructors
    public User() {}
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Listing> getListings() {
        return listings;
    }
    
    public void setListings(List<Listing> listings) {
        this.listings = listings;
    }
    
    public List<Transaction> getPurchases() {
        return purchases;
    }
    
    public void setPurchases(List<Transaction> purchases) {
        this.purchases = purchases;
    }
    
    public List<Wishlist> getWishlistItems() {
        return wishlistItems;
    }
    
    public void setWishlistItems(List<Wishlist> wishlistItems) {
        this.wishlistItems = wishlistItems;
    }
    
    public List<Message> getSentMessages() {
        return sentMessages;
    }
    
    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }
    
    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }
    
    public void setReceivedMessages(List<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }
    
    public List<Report> getReports() {
        return reports;
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    public List<Report> getModeratedReports() {
        return moderatedReports;
    }
    
    public void setModeratedReports(List<Report> moderatedReports) {
        this.moderatedReports = moderatedReports;
    }
    
    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    
    public List<Review> getReceivedReviews() {
        return receivedReviews;
    }
    
    public void setReceivedReviews(List<Review> receivedReviews) {
        this.receivedReviews = receivedReviews;
    }
    
    // Enums
    public enum UserRole {
        USER, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, SUSPENDED, BANNED
    }
}
