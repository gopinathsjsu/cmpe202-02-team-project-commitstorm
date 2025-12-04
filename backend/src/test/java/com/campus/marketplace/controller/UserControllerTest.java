package com.campus.marketplace.controller;

import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAdminAuthentication() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @Test
    void testGetUserById_Found() throws Exception {
        when(userService.getUserById("user-123")).thenReturn(Optional.of(testUser));
        
        mockMvc.perform(get("/api/users/user-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        
        verify(userService, times(1)).getUserById("user-123");
    }
    
    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/users/non-existent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(userService, times(1)).getUserById("non-existent");
    }
    
    @Test
    void testGetAllUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);
        setAdminAuthentication();
        
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
        
        verify(userService, times(1)).getAllUsers();
    }
    
    @Test
    void testGetUserByEmail() throws Exception {
        when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        
        mockMvc.perform(get("/api/users/email/john@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
        
        verify(userService, times(1)).getUserByEmail("john@example.com");
    }
    
    @Test
    void testGetUsersByRole() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.getUsersByRole(User.UserRole.USER)).thenReturn(users);
        
        mockMvc.perform(get("/api/users/role/USER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("USER"));
        
        verify(userService, times(1)).getUsersByRole(User.UserRole.USER);
    }
    
    @Test
    void testGetUsersByStatus() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.getUsersByStatus(User.UserStatus.ACTIVE)).thenReturn(users);
        
        mockMvc.perform(get("/api/users/status/ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        
        verify(userService, times(1)).getUsersByStatus(User.UserStatus.ACTIVE);
    }
    
    @Test
    void testSearchUsersByName() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.searchUsersByName("John")).thenReturn(users);
        
        mockMvc.perform(get("/api/users/search/name?name=John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }
    
    @Test
    void testUpdateUser() throws Exception {
        testUser.setName("Updated Name");
        when(userService.getUserById("user-123")).thenReturn(Optional.of(testUser));
        when(userService.updateUser(any(User.class))).thenReturn(testUser);
        
        mockMvc.perform(put("/api/users/user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }
    
    @Test
    void testDeleteUser() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(delete("/api/users/user-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(userService, times(1)).deleteUser("user-123");
    }
    
    @Test
    void testUserExistsByEmail() throws Exception {
        when(userService.existsByEmail("john@example.com")).thenReturn(true);
        
        mockMvc.perform(get("/api/users/exists/email?email=john@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
