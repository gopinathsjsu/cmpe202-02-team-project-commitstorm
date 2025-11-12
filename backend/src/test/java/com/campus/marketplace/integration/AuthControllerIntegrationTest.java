package com.campus.marketplace.integration;

import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController endpoints.
 */
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);
    }
    
    @Test
    void testRegister_Success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new RegisterRequest(
            "New User", "newuser@example.com", "password123"
        ));
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.user.name").value("New User"));
    }
    
    @Test
    void testRegister_DuplicateEmail() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new RegisterRequest(
            "Another User", "test@example.com", "password123"
        ));
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Email is already in use")));
    }
    
    @Test
    void testLogin_Success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(
            "test@example.com", "password123"
        ));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
    
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(
            "test@example.com", "wrongpassword"
        ));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }
    
    @Test
    void testGetCurrentUser_ValidToken() throws Exception {
        // First login to get token
        String loginBody = objectMapper.writeValueAsString(new LoginRequest(
            "test@example.com", "password123"
        ));
        
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String token = objectMapper.readTree(response).get("token").asText();
        
        // Use token to get current user
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
    
    @Test
    void testGetCurrentUser_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testGetCurrentUser_MissingHeader() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
    
    // Helper classes for request DTOs
    @SuppressWarnings("unused")
    private static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        
        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }
    
    @SuppressWarnings("unused")
    private static class LoginRequest {
        public String email;
        public String password;
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}

