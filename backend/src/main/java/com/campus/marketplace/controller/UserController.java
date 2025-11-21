package com.campus.marketplace.controller;

import com.campus.marketplace.dto.UserDTO;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User endpoints: CRUD and search (email/name/role/status).
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a user.
     * @param userDTO name, email, role, status
     * @return 201 with UserDTO
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : User.UserRole.USER);
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : User.UserStatus.ACTIVE);
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(createdUser));
    }
    
    /**
     * Get user by id.
     * @param id user id
     * @return 200 with UserDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * List all users.
     * Admin only.
     * @return 200 with list of UserDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        ensureAdminAccess();
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
      * Get user by email.
      * @param email email address
      * @return 200 with UserDTO or 404 if not found
      */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * List users by role.
     * @param role role enum
     * @return 200 with list of UserDTO
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable User.UserRole role) {
        List<UserDTO> users = userService.getUsersByRole(role).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * List users by status.
     * @param status status enum
     * @return 200 with list of UserDTO
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@PathVariable User.UserStatus status) {
        List<UserDTO> users = userService.getUsersByStatus(status).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Search users by name (partial match).
     * @param name search term
     * @return 200 with list of UserDTO
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<UserDTO>> searchUsersByName(@RequestParam String name) {
        List<UserDTO> users = userService.searchUsersByName(name).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Search users by email (partial match).
     * @param email search term
     * @return 200 with list of UserDTO
     */
    @GetMapping("/search/email")
    public ResponseEntity<List<UserDTO>> searchUsersByEmail(@RequestParam String email) {
        List<UserDTO> users = userService.searchUsersByEmail(email).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user fields.
     * @param id user id
     * @param userDTO name, email, role, status
     * @return 200 with updated UserDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @Valid @RequestBody UserDTO userDTO) {
        return userService.getUserById(id)
                .map(existingUser -> {
                    existingUser.setName(userDTO.getName());
                    existingUser.setEmail(userDTO.getEmail());
                    existingUser.setRole(userDTO.getRole());
                    existingUser.setStatus(userDTO.getStatus());
                    
                    User updatedUser = userService.updateUser(existingUser);
                    return ResponseEntity.ok(new UserDTO(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update user status only.
     * Admin only.
     * @param id user id
     * @param status new status
     * @return 200 with updated UserDTO or 404 if not found
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserStatus(@PathVariable String id, @RequestParam User.UserStatus status) {
        ensureAdminAccess();
        try {
            User updatedUser = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(new UserDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete user.
     * Admin only.
     * @param id user id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        ensureAdminAccess();
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check if email exists.
     * @param email email address
     * @return 200 with boolean exists
     */
    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    private void ensureAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
