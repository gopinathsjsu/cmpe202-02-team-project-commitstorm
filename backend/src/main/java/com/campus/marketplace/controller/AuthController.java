package com.campus.marketplace.controller;

import com.campus.marketplace.dto.AuthResponse;
import com.campus.marketplace.dto.LoginRequest;
import com.campus.marketplace.dto.RegisterRequest;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if user already exists
            if (authService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Error: Email is already in use!");
            }
            
            // Create new user
            User user = authService.register(registerRequest);
            String token = jwtUtil.generateToken(user.getEmail());
            
            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());
            
            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // For JWT, logout is typically handled on the client side by removing the token
        return ResponseEntity.ok("Successfully logged out");
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing or invalid");
            }
            
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            
            String email = jwtUtil.extractUsername(token);
            User user = authService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            
            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }
}
