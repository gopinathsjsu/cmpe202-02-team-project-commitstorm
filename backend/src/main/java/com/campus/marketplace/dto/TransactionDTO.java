package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Transaction: used for requests/responses.
 */
public class TransactionDTO {
    
    private String id;
    private String listingId;
    private String listingTitle;
    private String buyerId;
    private String buyerName;
    private String sellerId;
    private String sellerName;
    
    @NotNull(message = "Final price is required")
    @DecimalMin(value = "0.0", message = "Final price must be non-negative")
    private BigDecimal finalPrice;
    
    private Transaction.TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    /** No-args constructor for serialization. */
    public TransactionDTO() {}
    
    /**
     * Map from Transaction entity to DTO.
     * @param transaction source entity
     */
    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.listingId = transaction.getListing().getId();
        this.listingTitle = transaction.getListing().getTitle();
        this.buyerId = transaction.getBuyer().getId();
        this.buyerName = transaction.getBuyer().getName();
        this.sellerId = transaction.getListing().getSeller().getId();
        this.sellerName = transaction.getListing().getSeller().getName();
        this.finalPrice = transaction.getFinalPrice();
        this.status = transaction.getStatus();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
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
    
    public String getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
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
    
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
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
}
