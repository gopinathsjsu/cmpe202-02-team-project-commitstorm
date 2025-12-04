package com.campus.marketplace.repository;

import com.campus.marketplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with common finders.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /** Find user by email. */
    Optional<User> findByEmail(String email);
    
    /** List users by role. */
    List<User> findByRole(User.UserRole role);
    
    /** List users by status. */
    List<User> findByStatus(User.UserStatus status);
    
    /** Search users by name (partial). */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    /** Search users by email (partial). */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);
    
    /** Check email existence. */
    boolean existsByEmail(String email);
}
