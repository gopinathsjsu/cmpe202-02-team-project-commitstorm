package com.campus.marketplace.service;

import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create (or persist) a user, generating id if absent.
     * @param user user entity
     * @return saved User
     */
    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        return userRepository.save(user);
    }
    
    /**
     * Get user by id.
     * @param id user id
     * @return Optional user
     */
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by email.
     * @param email email
     * @return Optional user
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * List all users.
     * @return list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * List users by role.
     * @param role role
     * @return list of users
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * List users by status.
     * @param status status
     * @return list of users
     */
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    /**
     * Search users by name (partial).
     * @param name search term
     * @return list of users
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    /**
     * Search users by email (partial).
     * @param email search term
     * @return list of users
     */
    public List<User> searchUsersByEmail(String email) {
        return userRepository.findByEmailContaining(email);
    }
    
    /**
     * Update an existing user.
     * @param user user entity
     * @return saved User
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Delete user by id.
     * @param id user id
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    /**
     * Check email existence.
     * @param email email
     * @return true if exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Update user status.
     * @param id user id
     * @param status new status
     * @return updated User
     * @throws RuntimeException if not found
     */
    public User updateUserStatus(String id, User.UserStatus status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with id: " + id);
    }
}
