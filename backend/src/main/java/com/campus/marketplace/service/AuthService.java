package com.campus.marketplace.service;

import com.campus.marketplace.dto.RegisterRequest;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User register(RegisterRequest registerRequest) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(User.UserRole.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        
        return userRepository.save(user);
    }
    
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }
        
        return user;
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
