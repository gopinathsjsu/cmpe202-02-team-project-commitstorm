package com.campus.marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "listings")
public class Listing {
    
    @Id
    @UuidGenerator
    @Column(name = "id", length = 36)
    private String id;
    
    @NotNull(message = "Seller is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "`condition`", nullable = false)
    private ItemCondition condition;
    
    @Column(name = "images", columnDefinition = "JSON")
    private String images; // JSON string for image URLs
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wishlist> wishlistItems;
    
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;
    
    @OneToOne(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Transaction transaction;
    
    // Constructors
    public Listing() {}
    
    public Listing(User seller, String title, BigDecimal price, Category category, ItemCondition condition) {
        this.seller = seller;
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public ItemCondition getCondition() {
        return condition;
    }
    
    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public ListingStatus getStatus() {
        return status;
    }
    
    public void setStatus(ListingStatus status) {
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
    
    public List<Wishlist> getWishlistItems() {
        return wishlistItems;
    }
    
    public void setWishlistItems(List<Wishlist> wishlistItems) {
        this.wishlistItems = wishlistItems;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    
    // Enums
    public enum ItemCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR
    }
    
    public enum ListingStatus {
        ACTIVE, SOLD, PENDING, DRAFT, DISABLED
    }
}
