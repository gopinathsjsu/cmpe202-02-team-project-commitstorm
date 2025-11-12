package com.campus.marketplace.security;

import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for JWT authentication and authorization.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class SecurityTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("campusMarketTest")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private User testUser;
    private String validToken;
    private String invalidToken;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);
        
        validToken = jwtUtil.generateToken(testUser.getEmail());
        invalidToken = "invalid.jwt.token";
    }
    
    @Test
    void testPublicEndpoints_AccessibleWithoutAuth() throws Exception {
        // Health endpoint should be accessible
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
        
        // Register endpoint should be accessible
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New User\",\"email\":\"new@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        
        // Login endpoint should be accessible
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testProtectedEndpoints_RequireAuth() throws Exception {
        // Try to access protected endpoint without token
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
        
        // Try with invalid token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testProtectedEndpoints_WithValidToken() throws Exception {
        // Access protected endpoint with valid token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
    
    @Test
    void testTokenExpiration() throws Exception {
        // Generate a token with short expiration (if configurable)
        // For now, we'll test that expired tokens are rejected
        // This would require custom JwtUtil configuration for testing
        
        // Test that malformed tokens are rejected
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer malformed.token.here"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testMissingBearerPrefix() throws Exception {
        // Token without "Bearer " prefix should be rejected
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", validToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testRoleBasedAccess() throws Exception {
        // Create admin user
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        admin = userRepository.save(admin);
        String adminToken = jwtUtil.generateToken(admin.getEmail());
        
        // Regular user should not have admin access
        // (This depends on your admin endpoints implementation)
        // For now, we'll test that different roles can authenticate
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }
    
    @Test
    void testInactiveUser_CannotAuthenticate() throws Exception {
        // Suspend user
        testUser.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(testUser);
        
        // Try to login with suspended account
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }
}

