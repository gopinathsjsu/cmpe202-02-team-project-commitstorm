package com.campus.marketplace.service;

import com.campus.marketplace.entity.Follow;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.FollowRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FollowService {
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Follow a seller.
     * @param followerId the user who wants to follow
     * @param sellerId the seller to follow
     * @return the created Follow entity
     * @throws RuntimeException if user or seller not found, or already following
     */
    public Follow followSeller(String followerId, String sellerId) {
        // Validate that both users exist
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        
        if (followerOpt.isEmpty()) {
            throw new RuntimeException("Follower not found with id: " + followerId);
        }
        if (sellerOpt.isEmpty()) {
            throw new RuntimeException("Seller not found with id: " + sellerId);
        }
        
        // Check if user is trying to follow themselves
        if (followerId.equals(sellerId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        
        // Check if already following
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndSellerId(followerId, sellerId);
        if (existingFollow.isPresent()) {
            throw new RuntimeException("Already following this seller");
        }
        
        // Create and save the follow relationship
        Follow follow = new Follow();
        follow.setId(UUID.randomUUID().toString());
        follow.setFollowerId(followerId);
        follow.setSellerId(sellerId);
        follow.setFollower(followerOpt.get());
        follow.setSeller(sellerOpt.get());
        
        return followRepository.save(follow);
    }
    
    /**
     * Unfollow a seller.
     * @param followerId the user who wants to unfollow
     * @param sellerId the seller to unfollow
     * @throws RuntimeException if follow relationship not found
     */
    public void unfollowSeller(String followerId, String sellerId) {
        Optional<Follow> followOpt = followRepository.findByFollowerIdAndSellerId(followerId, sellerId);
        if (followOpt.isEmpty()) {
            throw new RuntimeException("Not following this seller");
        }
        
        followRepository.delete(followOpt.get());
    }
    
    /**
     * Check if a user follows a seller.
     * @param followerId the user id
     * @param sellerId the seller id
     * @return true if following, false otherwise
     */
    public boolean isFollowing(String followerId, String sellerId) {
        return followRepository.findByFollowerIdAndSellerId(followerId, sellerId).isPresent();
    }
    
    /**
     * Get all sellers followed by a user.
     * @param followerId the user id
     * @return list of Follow entities
     */
    public List<Follow> getFollowedSellers(String followerId) {
        return followRepository.findByFollowerId(followerId);
    }
    
    /**
     * Get all followers of a seller.
     * @param sellerId the seller id
     * @return list of Follow entities
     */
    public List<Follow> getFollowers(String sellerId) {
        return followRepository.findBySellerId(sellerId);
    }
    
    /**
     * Get follower count for a seller.
     * @param sellerId the seller id
     * @return number of followers
     */
    public Long getFollowerCount(String sellerId) {
        return followRepository.countBySellerId(sellerId);
    }
    
    /**
     * Get following count for a user.
     * @param followerId the user id
     * @return number of sellers being followed
     */
    public Long getFollowingCount(String followerId) {
        return followRepository.countByFollowerId(followerId);
    }
}

