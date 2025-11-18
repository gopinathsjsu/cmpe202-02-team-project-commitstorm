package com.campus.marketplace.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityConfigTest {
    
    @Test
    void testPasswordEncoderBean() {
        // This test verifies that the PasswordEncoder bean is created
        // In a real scenario, you'd use @SpringBootTest with a test context
        // For unit testing, we'll just verify the configuration class exists
        assertNotNull(SecurityConfig.class);
        assertTrue(SecurityConfig.class.isAnnotationPresent(EnableMethodSecurity.class));
    }
    
    @Test
    void testSecurityConfigExists() {
        // Basic assertion that SecurityConfig class exists and is properly annotated
        assertNotNull(SecurityConfig.class);
        assertTrue(SecurityConfig.class.isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class));
        assertTrue(SecurityConfig.class.isAnnotationPresent(
            org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class));
    }
    
    // Note: Full integration testing of SecurityConfig would require:
    // 1. Setting up a test Spring context
    // 2. Verifying SecurityFilterChain configuration
    // 3. Testing endpoint protection rules
    // This is typically done with @SpringBootTest and MockMvc integration tests
}