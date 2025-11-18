package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Follow;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FollowControllerTest {
    
    @Mock
    private FollowService followService;
    
    @InjectMocks
    private FollowController followController;
    
    private MockMvc mockMvc;
    private Follow testFollow;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(followController).build();
        
        testFollow = new Follow();
        testFollow.setId("follow-123");
        testFollow.setFollowerId("follower-123");
        testFollow.setSellerId("seller-123");
    }
    
    @Test
    void testFollowUser() throws Exception {
        when(followService.followSeller("follower-123", "seller-123")).thenReturn(testFollow);

        mockMvc.perform(post("/api/follows")
            .param("followerId", "follower-123")
            .param("sellerId", "seller-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.followerId").value("follower-123"))
            .andExpect(jsonPath("$.sellerId").value("seller-123"));

        verify(followService, times(1)).followSeller("follower-123", "seller-123");
    }
    
    @Test
    void testFollowUser_AlreadyFollowing() throws Exception {
        when(followService.followSeller("follower-123", "seller-123"))
            .thenThrow(new RuntimeException("Already following"));

        mockMvc.perform(post("/api/follows")
                .param("followerId", "follower-123")
                .param("sellerId", "seller-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testUnfollowUser() throws Exception {
        mockMvc.perform(delete("/api/follows")
            .param("followerId", "follower-123")
            .param("sellerId", "seller-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(followService, times(1)).unfollowSeller("follower-123", "seller-123");
    }
    
    @Test
    void testGetFollowers() throws Exception {
        List<Follow> followers = Arrays.asList(testFollow);
        when(followService.getFollowers("seller-123")).thenReturn(followers);

        mockMvc.perform(get("/api/follows/followers/seller-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].followerId").value("follower-123"));
    }
    
    @Test
    void testGetFollowing() throws Exception {
        List<Follow> following = Arrays.asList(testFollow);
        when(followService.getFollowedSellers("follower-123")).thenReturn(following);

        mockMvc.perform(get("/api/follows/following/follower-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].sellerId").value("seller-123"));
    }
    
    @Test
    void testIsFollowing() throws Exception {
        when(followService.isFollowing("follower-123", "seller-123")).thenReturn(true);

        mockMvc.perform(get("/api/follows/check")
            .param("followerId", "follower-123")
            .param("sellerId", "seller-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
