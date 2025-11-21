package com.campus.marketplace.config;

import com.campus.marketplace.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private UserDetails userDetails;
    private String validToken;
    private String validUsername;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        validToken = "valid-jwt-token";
        validUsername = "user@example.com";
        
        userDetails = User.builder()
                .username(validUsername)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }
    
    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(true);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_InvalidHeaderFormat() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtil.extractUsername(invalidToken)).thenThrow(new RuntimeException("Invalid token"));
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_ExpiredToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(false);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_AlreadyAuthenticated() throws Exception {
        // Arrange
        // Set up existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        // Should not load user details if already authenticated
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}