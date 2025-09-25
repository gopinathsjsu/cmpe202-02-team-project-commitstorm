package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, String> {
    
    List<Listing> findBySellerId(String sellerId);
    
    List<Listing> findByCategoryId(String categoryId);
    
    List<Listing> findByStatus(Listing.ListingStatus status);
    
    List<Listing> findByCondition(Listing.ItemCondition condition);
    
    @Query("SELECT l FROM Listing l WHERE l.price BETWEEN :minPrice AND :maxPrice")
    List<Listing> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT l FROM Listing l WHERE l.title LIKE %:title% OR l.description LIKE %:description%")
    List<Listing> findByTitleOrDescriptionContaining(@Param("title") String title, @Param("description") String description);
    
    @Query("SELECT l FROM Listing l WHERE l.seller.id = :sellerId AND l.status = :status")
    List<Listing> findBySellerIdAndStatus(@Param("sellerId") String sellerId, @Param("status") Listing.ListingStatus status);
    
    @Query("SELECT l FROM Listing l WHERE l.category.id = :categoryId AND l.status = :status")
    Page<Listing> findByCategoryIdAndStatus(@Param("categoryId") String categoryId, @Param("status") Listing.ListingStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<Listing> findByStatusOrderByCreatedAtDesc(@Param("status") Listing.ListingStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE l.seller.id = :sellerId ORDER BY l.createdAt DESC")
    Page<Listing> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") String sellerId, Pageable pageable);
}
