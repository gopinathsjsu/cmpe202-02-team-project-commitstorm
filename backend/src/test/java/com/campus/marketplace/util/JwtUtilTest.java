package com.campus.marketplace.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {
    
    @InjectMocks
    private JwtUtil jwtUtil;
    
    private String testSecret;
    private Long testExpiration;
    
    @BeforeEach
    void setUp() {
        testSecret = "mySecretKeyThatIsLongEnoughForHS256AlgorithmAt32CharactersMinimum";
        testExpiration = 86400000L; // 24 hours
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }
    
    @Test
    void testGenerateToken_WithString() {
        String token = jwtUtil.generateToken("test@example.com");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
    
    @Test
    void testGenerateToken_WithUserDetails() {
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken("test@example.com");
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("test@example.com", username);
    }
    
    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken("test@example.com");
        Date expiration = jwtUtil.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateToken("test@example.com");
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtUtil.generateToken("test@example.com");
        UserDetails userDetails = new User("different@example.com", "password", new ArrayList<>());
        
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Negative expiration
        String token = jwtUtil.generateToken("test@example.com");
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertFalse(isValid);
    }
    
    
    @Test
    void testExtractClaim() {
        String token = jwtUtil.generateToken("test@example.com");
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        
        assertEquals("test@example.com", subject);
    }
    
    @Test
    void testInvalidToken_MalformedJWT() {
        assertThrows(Exception.class, () -> 
            jwtUtil.extractUsername("invalid.token.here")
        );
    }
}
