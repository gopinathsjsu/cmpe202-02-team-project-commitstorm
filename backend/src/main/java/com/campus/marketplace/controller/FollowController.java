package com.campus.marketplace.controller;

import com.campus.marketplace.dto.FollowDTO;
import com.campus.marketplace.entity.Follow;
import com.campus.marketplace.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follows")
@CrossOrigin(origins = "*")
public class FollowController {
    
    @Autowired
    private FollowService followService;
    
    /**
     * Follow a seller.
     * @param followerId the user who wants to follow
     * @param sellerId the seller to follow
     * @return 201 with FollowDTO or 400 on validation failure
     */
    @PostMapping
    public ResponseEntity<FollowDTO> followSeller(
            @RequestParam String followerId,
            @RequestParam String sellerId) {
        try {
            Follow follow = followService.followSeller(followerId, sellerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FollowDTO(follow));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Unfollow a seller.
     * @param followerId the user who wants to unfollow
     * @param sellerId the seller to unfollow
     * @return 204 No Content or 400 on failure
     */
    @DeleteMapping
    public ResponseEntity<Void> unfollowSeller(
            @RequestParam String followerId,
            @RequestParam String sellerId) {
        try {
            followService.unfollowSeller(followerId, sellerId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check if a user follows a seller.
     * @param followerId the user id
     * @param sellerId the seller id
     * @return 200 with boolean
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> isFollowing(
            @RequestParam String followerId,
            @RequestParam String sellerId) {
        boolean isFollowing = followService.isFollowing(followerId, sellerId);
        return ResponseEntity.ok(isFollowing);
    }
    
    /**
     * Get all sellers followed by a user.
     * @param followerId the user id
     * @return 200 with list of FollowDTO
     */
    @GetMapping("/following/{followerId}")
    public ResponseEntity<List<FollowDTO>> getFollowedSellers(@PathVariable String followerId) {
        List<FollowDTO> follows = followService.getFollowedSellers(followerId).stream()
                .map(FollowDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(follows);
    }
    
    /**
     * Get all followers of a seller.
     * @param sellerId the seller id
     * @return 200 with list of FollowDTO
     */
    @GetMapping("/followers/{sellerId}")
    public ResponseEntity<List<FollowDTO>> getFollowers(@PathVariable String sellerId) {
        List<FollowDTO> follows = followService.getFollowers(sellerId).stream()
                .map(FollowDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(follows);
    }
    
    /**
     * Get follower count for a seller.
     * @param sellerId the seller id
     * @return 200 with count
     */
    @GetMapping("/followers/{sellerId}/count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable String sellerId) {
        Long count = followService.getFollowerCount(sellerId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get following count for a user.
     * @param followerId the user id
     * @return 200 with count
     */
    @GetMapping("/following/{followerId}/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable String followerId) {
        Long count = followService.getFollowingCount(followerId);
        return ResponseEntity.ok(count);
    }
}

