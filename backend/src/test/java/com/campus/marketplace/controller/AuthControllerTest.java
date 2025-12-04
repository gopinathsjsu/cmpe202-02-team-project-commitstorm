package com.campus.marketplace.controller;

import com.campus.marketplace.dto.LoginRequest;
import com.campus.marketplace.dto.RegisterRequest;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    
    @Mock
    private AuthService authService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthController authController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$hashedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
    }
    
    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        when(authService.existsByEmail("john@example.com")).thenReturn(false);
        when(authService.register(any(RegisterRequest.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("test-jwt-token");
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
        
        verify(authService).existsByEmail("john@example.com");
        verify(authService).register(any(RegisterRequest.class));
        verify(jwtUtil).generateToken("john@example.com");
    }
    
    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        // Arrange
        when(authService.existsByEmail("john@example.com")).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email is already in use!"));
        
        verify(authService).existsByEmail("john@example.com");
        verify(authService, never()).register(any(RegisterRequest.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }
    
    @Test
    void testRegister_ServiceException() throws Exception {
        // Arrange
        when(authService.existsByEmail("john@example.com")).thenReturn(false);
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Database error"));
        
        verify(authService).existsByEmail("john@example.com");
        verify(authService).register(any(RegisterRequest.class));
    }
    
    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        when(authService.authenticate("john@example.com", "password123")).thenReturn(testUser);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("test-jwt-token");
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        
        verify(authService).authenticate("john@example.com", "password123");
        verify(jwtUtil).generateToken("john@example.com");
    }
    
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        when(authService.authenticate("john@example.com", "wrongPassword"))
                .thenThrow(new RuntimeException("Invalid password"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("john@example.com", "wrongPassword"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Invalid password"));
        
        verify(authService).authenticate("john@example.com", "wrongPassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }
    
    @Test
    void testLogin_UserNotFound() throws Exception {
        // Arrange
        when(authService.authenticate("nonexistent@example.com", "password123"))
                .thenThrow(new RuntimeException("User not found"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("nonexistent@example.com", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: User not found"));
        
        verify(authService).authenticate("nonexistent@example.com", "password123");
    }
    
    @Test
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }
    
    @Test
    void testGetCurrentUser_Success() throws Exception {
        // Arrange
        String token = "test-jwt-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("john@example.com");
        when(authService.getUserByEmail("john@example.com")).thenReturn(testUser);
        
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(authService).getUserByEmail("john@example.com");
    }
    
    @Test
    void testGetCurrentUser_MissingHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authorization header missing or invalid"));
        
        verify(jwtUtil, never()).validateToken(anyString());
    }
    
    @Test
    void testGetCurrentUser_InvalidHeaderFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authorization header missing or invalid"));
        
        verify(jwtUtil, never()).validateToken(anyString());
    }
    
    @Test
    void testGetCurrentUser_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid-token";
        when(jwtUtil.validateToken(token)).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
        
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
    
    @Test
    void testGetCurrentUser_UserNotFound() throws Exception {
        // Arrange
        String token = "test-jwt-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("nonexistent@example.com");
        when(authService.getUserByEmail("nonexistent@example.com"))
                .thenThrow(new RuntimeException("User not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Error: User not found"));
        
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(authService).getUserByEmail("nonexistent@example.com");
    }
}