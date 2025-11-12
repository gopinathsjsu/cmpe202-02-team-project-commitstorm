package com.campus.marketplace.service;

import com.campus.marketplace.dto.RegisterRequest;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }
    
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        User result = authService.register(request);
        
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }
    
    @Test
    void testAuthenticate_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        
        User result = authService.authenticate("test@example.com", "password123");
        
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }
    
    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("nonexistent@example.com", "password123");
        });
    }
    
    @Test
    void testAuthenticate_InvalidPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("test@example.com", "wrongpassword");
        });
    }
    
    @Test
    void testAuthenticate_InactiveUser() {
        testUser.setStatus(User.UserStatus.SUSPENDED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("test@example.com", "password123");
        });
    }
    
    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        User result = authService.getUserByEmail("test@example.com");
        
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }
    
    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            authService.getUserByEmail("nonexistent@example.com");
        });
    }
    
    @Test
    void testExistsByEmail_True() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        boolean result = authService.existsByEmail("test@example.com");
        
        assertTrue(result);
    }
    
    @Test
    void testExistsByEmail_False() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
        
        boolean result = authService.existsByEmail("nonexistent@example.com");
        
        assertFalse(result);
    }
}

