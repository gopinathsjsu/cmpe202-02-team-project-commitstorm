package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    
    // Check if a user follows a seller
    Optional<Follow> findByFollowerIdAndSellerId(String followerId, String sellerId);
    
    // Get all sellers followed by a user
    @Query("SELECT f FROM Follow f WHERE f.followerId = :followerId")
    List<Follow> findByFollowerId(@Param("followerId") String followerId);
    
    // Get all followers of a seller
    @Query("SELECT f FROM Follow f WHERE f.sellerId = :sellerId")
    List<Follow> findBySellerId(@Param("sellerId") String sellerId);
    
    // Count followers of a seller
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.sellerId = :sellerId")
    Long countBySellerId(@Param("sellerId") String sellerId);
    
    // Count sellers followed by a user
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followerId = :followerId")
    Long countByFollowerId(@Param("followerId") String followerId);
}

