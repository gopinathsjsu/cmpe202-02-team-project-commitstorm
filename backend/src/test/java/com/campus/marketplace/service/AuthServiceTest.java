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

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private RegisterRequest registerRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$hashedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
    }
    
    @Test
    void testRegister_Success() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-123");
            return user;
        });
        
        // Act
        User result = authService.register(registerRequest);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(User.UserRole.USER, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getPassword());
        assertNotEquals("password123", result.getPassword()); // Password should be hashed
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegister_PasswordHashing() {
        // Arrange
        String hashedPassword = "$2a$10$hashedPassword";
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        User result = authService.register(registerRequest);
        
        // Assert
        assertEquals(hashedPassword, result.getPassword());
        verify(passwordEncoder).encode("password123");
    }
    
    @Test
    void testAuthenticate_Success() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        
        // Act
        User result = authService.authenticate("john@example.com", "password123");
        
        // Assert
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }
    
    @Test
    void testAuthenticate_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("nonexistent@example.com", "password123");
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testAuthenticate_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("john@example.com", "wrongPassword");
        });
        
        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("wrongPassword", testUser.getPassword());
    }
    
    @Test
    void testAuthenticate_InactiveUser() {
        // Arrange
        testUser.setStatus(User.UserStatus.SUSPENDED);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("john@example.com", "password123");
        });
        
        assertEquals("User account is not active", exception.getMessage());
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }
    
    @Test
    void testAuthenticate_BannedUser() {
        // Arrange
        testUser.setStatus(User.UserStatus.BANNED);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate("john@example.com", "password123");
        });
        
        assertEquals("User account is not active", exception.getMessage());
    }
    
    @Test
    void testGetUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        
        // Act
        User result = authService.getUserByEmail("john@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findByEmail("john@example.com");
    }
    
    @Test
    void testGetUserByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserByEmail("nonexistent@example.com");
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }
    
    @Test
    void testExistsByEmail_True() {
        // Arrange
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        
        // Act
        boolean result = authService.existsByEmail("john@example.com");
        
        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("john@example.com");
    }
    
    @Test
    void testExistsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
        
        // Act
        boolean result = authService.existsByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
}