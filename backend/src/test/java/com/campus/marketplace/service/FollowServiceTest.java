package com.campus.marketplace.service;

import com.campus.marketplace.entity.Follow;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.FollowRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {
    
    @Mock
    private FollowRepository followRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private FollowService followService;
    
    private Follow testFollow;
    private User follower;
    private User seller;
    
    @BeforeEach
    void setUp() {
        follower = new User();
        follower.setId("follower-123");
        follower.setName("Follower");
        follower.setEmail("follower@example.com");
        
        seller = new User();
        seller.setId("seller-123");
        seller.setName("Seller");
        seller.setEmail("seller@example.com");
        
        testFollow = new Follow();
        testFollow.setId("follow-123");
        testFollow.setFollowerId("follower-123");
        testFollow.setSellerId("seller-123");
    }
    
    @Test
    void testFollowUser_Success() {
        when(userRepository.findById("follower-123")).thenReturn(Optional.of(follower));
        when(userRepository.findById("seller-123")).thenReturn(Optional.of(seller));
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123"))
            .thenReturn(Optional.empty());
        when(followRepository.save(any(Follow.class))).thenReturn(testFollow);

        Follow result = followService.followSeller("follower-123", "seller-123");

        assertNotNull(result);
        assertEquals("follower-123", result.getFollowerId());
        assertEquals("seller-123", result.getSellerId());
        verify(followRepository, times(1)).save(any(Follow.class));
    }
    
    @Test
    void testFollowUser_FollowerNotFound() {
        when(userRepository.findById("follower-999")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            followService.followSeller("follower-999", "followee-123")
        );
        
        verify(followRepository, never()).save(any(Follow.class));
    }
    
    @Test
    void testFollowUser_FolloweeNotFound() {
        when(userRepository.findById("follower-123")).thenReturn(Optional.of(follower));
        when(userRepository.findById("seller-999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            followService.followSeller("follower-123", "seller-999")
        );
    }
    
    @Test
    void testFollowUser_AlreadyFollowing() {
        when(userRepository.findById("follower-123")).thenReturn(Optional.of(follower));
        when(userRepository.findById("seller-123")).thenReturn(Optional.of(seller));
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123"))
            .thenReturn(Optional.of(testFollow));

        assertThrows(RuntimeException.class, () -> 
            followService.followSeller("follower-123", "seller-123")
        );
    }
    
    @Test
    void testUnfollowUser_Success() {
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123")).thenReturn(Optional.of(testFollow));

        assertDoesNotThrow(() -> 
            followService.unfollowSeller("follower-123", "seller-123")
        );

        verify(followRepository, times(1)).delete(testFollow);
    }
    
    @Test
    void testUnfollowUser_NotFollowing() {
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            followService.unfollowSeller("follower-123", "seller-123")
        );
    }
    
    @Test
    void testGetFollowers() {
        List<Follow> followers = Arrays.asList(testFollow);
        when(followRepository.findBySellerId("seller-123")).thenReturn(followers);

        List<Follow> result = followService.getFollowers("seller-123");

        assertEquals(1, result.size());
    }
    
    @Test
    void testGetFollowing() {
        List<Follow> following = Arrays.asList(testFollow);
        when(followRepository.findByFollowerId("follower-123")).thenReturn(following);

        List<Follow> result = followService.getFollowedSellers("follower-123");

        assertEquals(1, result.size());
    }
    
    @Test
    void testIsFollowing_True() {
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123")).thenReturn(Optional.of(testFollow));

        boolean result = followService.isFollowing("follower-123", "seller-123");

        assertTrue(result);
    }
    
    @Test
    void testIsFollowing_False() {
        when(followRepository.findByFollowerIdAndSellerId("follower-123", "seller-123")).thenReturn(Optional.empty());

        boolean result = followService.isFollowing("follower-123", "seller-123");

        assertFalse(result);
    }
}
