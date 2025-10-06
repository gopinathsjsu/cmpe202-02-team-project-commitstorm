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

/**
 * Repository for Listing entity with filters and paging queries.
 */
@Repository
public interface ListingRepository extends JpaRepository<Listing, String> {
    
    /** List listings by seller. */
    List<Listing> findBySellerId(String sellerId);
    
    /** List listings by category. */
    List<Listing> findByCategoryId(String categoryId);
    
    /** List listings by status. */
    List<Listing> findByStatus(Listing.ListingStatus status);
    
    /** List listings by condition. */
    List<Listing> findByCondition(Listing.ItemCondition condition);
    
    /** List listings in a price range. */
    @Query("SELECT l FROM Listing l WHERE l.price BETWEEN :minPrice AND :maxPrice")
    List<Listing> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    /** Search listings by title or description (partial). */
    @Query("SELECT l FROM Listing l WHERE l.title LIKE %:title% OR l.description LIKE %:description%")
    List<Listing> findByTitleOrDescriptionContaining(@Param("title") String title, @Param("description") String description);
    
    /** List listings by seller and status. */
    @Query("SELECT l FROM Listing l WHERE l.seller.id = :sellerId AND l.status = :status")
    List<Listing> findBySellerIdAndStatus(@Param("sellerId") String sellerId, @Param("status") Listing.ListingStatus status);
    
    /** Page listings by category and status. */
    @Query("SELECT l FROM Listing l WHERE l.category.id = :categoryId AND l.status = :status")
    Page<Listing> findByCategoryIdAndStatus(@Param("categoryId") String categoryId, @Param("status") Listing.ListingStatus status, Pageable pageable);
    
    /** Page listings by status ordered by creation time desc. */
    @Query("SELECT l FROM Listing l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<Listing> findByStatusOrderByCreatedAtDesc(@Param("status") Listing.ListingStatus status, Pageable pageable);
    
    /** Page listings by seller ordered by creation time desc. */
    @Query("SELECT l FROM Listing l WHERE l.seller.id = :sellerId ORDER BY l.createdAt DESC")
    Page<Listing> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") String sellerId, Pageable pageable);
}
