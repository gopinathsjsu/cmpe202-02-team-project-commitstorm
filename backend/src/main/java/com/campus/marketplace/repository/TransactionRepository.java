package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findByBuyerId(String buyerId);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    Optional<Transaction> findByListingId(String listingId);
    
    @Query("SELECT t FROM Transaction t WHERE t.buyer.id = :buyerId ORDER BY t.createdAt DESC")
    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(@Param("buyerId") String buyerId);
    
    @Query("SELECT t FROM Transaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByStatusOrderByCreatedAtDesc(@Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.listing.seller.id = :sellerId ORDER BY t.createdAt DESC")
    List<Transaction> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") String sellerId);
    
    @Query("SELECT t FROM Transaction t WHERE t.listing.seller.id = :sellerId AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findBySellerIdAndStatusOrderByCreatedAtDesc(@Param("sellerId") String sellerId, @Param("status") Transaction.TransactionStatus status);
}
