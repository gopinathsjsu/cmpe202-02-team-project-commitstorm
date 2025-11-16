package com.campus.marketplace.service;

import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }
    
    @Test
    void testLoadUserByUsername_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");
        
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }
    
    @Test
    void testLoadUserByUsername_ActiveUser_Enabled() {
        testUser.setStatus(User.UserStatus.ACTIVE);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");
        
        assertTrue(userDetails.isEnabled());
    }
    
    @Test
    void testLoadUserByUsername_InactiveUser_Disabled() {
        testUser.setStatus(User.UserStatus.SUSPENDED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");
        
        assertFalse(userDetails.isEnabled());
    }
    
    @Test
    void testLoadUserByUsername_UserRole() {
        testUser.setRole(User.UserRole.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
    
    @Test
    void testLoadUserByUsername_AdminRole() {
        testUser.setRole(User.UserRole.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
    
    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("nonexistent@example.com")
        );
    }
    
    @Test
    void testLoadUserByUsername_ExceptionMessage() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername(email)
        );
        
        assertTrue(exception.getMessage().contains(email));
    }
}
