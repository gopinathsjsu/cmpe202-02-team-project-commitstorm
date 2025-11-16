package com.campus.marketplace.service;

import com.campus.marketplace.entity.User;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }
    
    @Test
    void testCreateUser_WithoutId_GeneratesUUID() {
        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("hashedPassword");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotNull(savedUser.getId());
            return savedUser;
        });
        
        User result = userService.createUser(user);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testCreateUser_WithId_DoesNotOverrideId() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.createUser(testUser);
        
        assertEquals("user-123", result.getId());
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void testGetUserById_Found() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserById("user-123");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository, times(1)).findById("user-123");
    }
    
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        Optional<User> result = userService.getUserById("non-existent");
        
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById("non-existent");
    }
    
    @Test
    void testGetUserByEmail_Found() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("john@example.com");
        
        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }
    
    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetAllUsers() {
        User user2 = new User();
        user2.setId("user-456");
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");
        user2.setRole(User.UserRole.USER);
        user2.setStatus(User.UserStatus.ACTIVE);
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);
        
        List<User> result = userService.getAllUsers();
        
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void testGetUsersByRole() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(User.UserRole.USER)).thenReturn(users);
        
        List<User> result = userService.getUsersByRole(User.UserRole.USER);
        
        assertEquals(1, result.size());
        assertEquals(User.UserRole.USER, result.get(0).getRole());
        verify(userRepository, times(1)).findByRole(User.UserRole.USER);
    }
    
    @Test
    void testGetUsersByStatus() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByStatus(User.UserStatus.ACTIVE)).thenReturn(users);
        
        List<User> result = userService.getUsersByStatus(User.UserStatus.ACTIVE);
        
        assertEquals(1, result.size());
        assertEquals(User.UserStatus.ACTIVE, result.get(0).getStatus());
        verify(userRepository, times(1)).findByStatus(User.UserStatus.ACTIVE);
    }
    
    @Test
    void testSearchUsersByName() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByNameContaining("John")).thenReturn(users);
        
        List<User> result = userService.searchUsersByName("John");
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("John"));
        verify(userRepository, times(1)).findByNameContaining("John");
    }
    
    @Test
    void testSearchUsersByEmail() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByEmailContaining("john")).thenReturn(users);
        
        List<User> result = userService.searchUsersByEmail("john");
        
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByEmailContaining("john");
    }
    
    @Test
    void testUpdateUser() {
        testUser.setName("Updated Name");
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        User result = userService.updateUser(testUser);
        
        assertEquals("Updated Name", result.getName());
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void testDeleteUser() {
        userService.deleteUser("user-123");
        
        verify(userRepository, times(1)).deleteById("user-123");
    }
    
    @Test
    void testUserExistsByEmail_True() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        
        boolean result = userService.existsByEmail("john@example.com");
        
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("john@example.com");
    }
    
    @Test
    void testUserExistsByEmail_False() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
        
        boolean result = userService.existsByEmail("nonexistent@example.com");
        
        assertFalse(result);
    }
}
