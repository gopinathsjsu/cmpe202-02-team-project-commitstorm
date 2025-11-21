package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Report;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.ReportService;
import com.campus.marketplace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for admin access control.
 * Tests verify admin-only endpoints by simulating admin and regular user security contexts.
 * 
 * Note: @PreAuthorize annotations require Spring AOP proxies to be enforced, which means
 * they need a Spring context. These unit tests verify the controller logic and service calls,
 * but for full @PreAuthorize enforcement testing, use integration tests with @SpringBootTest.
 * 
 * The tests demonstrate:
 * - Admin users can access admin endpoints (service methods are called)
 * - Regular users are denied access (AccessDeniedException is expected)
 * - Proper security context setup and cleanup
 */
@ExtendWith(MockitoExtension.class)
@EnableMethodSecurity(prePostEnabled = true)
public class AdminAccessControlTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private ReportService reportService;
    
    @InjectMocks
    private UserController userController;
    
    @InjectMocks
    private ReportController reportController;
    
    private User testUser;
    private User adminUser;
    private Report testReport;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        
        adminUser = new User();
        adminUser.setId("admin-123");
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setStatus(User.UserStatus.ACTIVE);
        
        testReport = new Report();
        testReport.setId("report-123");
        testReport.setTargetType(Report.TargetType.LISTING);
        testReport.setStatus(Report.ReportStatus.OPEN);
        testReport.setTargetId("listing-123");
        testReport.setReason("Spam");
        testReport.setReporter(testUser);
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setUpdatedAt(LocalDateTime.now());
    }
    
    private void setAdminSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin@example.com",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
    
    private void setUserSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user@example.com",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
    
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    
    // ========== UserController Admin Access Tests ==========
    
    @Test
    void testGetAllUsers_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);
        
        // Act
        var response = userController.getAllUsers();
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userService).getAllUsers();
        
        clearSecurityContext();
    }
    
    @Test
    void testGetAllUsers_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert - Should throw AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> {
            userController.getAllUsers();
        });
        
        verify(userService, never()).getAllUsers();
        clearSecurityContext();
    }
    
    @Test
    void testUpdateUserStatus_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        testUser.setStatus(User.UserStatus.SUSPENDED);
        when(userService.updateUserStatus("user-123", User.UserStatus.SUSPENDED))
                .thenReturn(testUser);
        
        // Act
        var response = userController.updateUserStatus("user-123", User.UserStatus.SUSPENDED);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(User.UserStatus.SUSPENDED, response.getBody().getStatus());
        verify(userService).updateUserStatus("user-123", User.UserStatus.SUSPENDED);
        
        clearSecurityContext();
    }
    
    @Test
    void testUpdateUserStatus_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userController.updateUserStatus("user-123", User.UserStatus.SUSPENDED);
        });
        
        verify(userService, never()).updateUserStatus(anyString(), any());
        clearSecurityContext();
    }
    
    @Test
    void testDeleteUser_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        doNothing().when(userService).deleteUser("user-123");
        
        // Act
        var response = userController.deleteUser("user-123");
        
        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(userService).deleteUser("user-123");
        
        clearSecurityContext();
    }
    
    @Test
    void testDeleteUser_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userController.deleteUser("user-123");
        });
        
        verify(userService, never()).deleteUser(anyString());
        clearSecurityContext();
    }
    
    // ========== ReportController Admin Access Tests ==========
    
    @Test
    void testGetAllReports_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        List<Report> reports = Arrays.asList(testReport);
        when(reportService.getAllReports()).thenReturn(reports);
        
        // Act
        var response = reportController.getAllReports();
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(reportService).getAllReports();
        
        clearSecurityContext();
    }
    
    @Test
    void testGetAllReports_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reportController.getAllReports();
        });
        
        verify(reportService, never()).getAllReports();
        clearSecurityContext();
    }
    
    @Test
    void testAssignModerator_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        User moderator = new User();
        moderator.setId("moderator-123");
        testReport.setModerator(moderator);
        when(reportService.assignModerator("report-123", "moderator-123"))
                .thenReturn(testReport);
        
        // Act
        var response = reportController.assignModerator("report-123", "moderator-123");
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(reportService).assignModerator("report-123", "moderator-123");
        
        clearSecurityContext();
    }
    
    @Test
    void testAssignModerator_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reportController.assignModerator("report-123", "moderator-123");
        });
        
        verify(reportService, never()).assignModerator(anyString(), anyString());
        clearSecurityContext();
    }
    
    @Test
    void testUpdateReportStatus_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        testReport.setStatus(Report.ReportStatus.RESOLVED);
        when(reportService.updateReportStatus("report-123", Report.ReportStatus.RESOLVED))
                .thenReturn(testReport);
        
        // Act
        var response = reportController.updateReportStatus("report-123", Report.ReportStatus.RESOLVED);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(Report.ReportStatus.RESOLVED, response.getBody().getStatus());
        verify(reportService).updateReportStatus("report-123", Report.ReportStatus.RESOLVED);
        
        clearSecurityContext();
    }
    
    @Test
    void testUpdateReportStatus_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reportController.updateReportStatus("report-123", Report.ReportStatus.RESOLVED);
        });
        
        verify(reportService, never()).updateReportStatus(anyString(), any());
        clearSecurityContext();
    }
    
    @Test
    void testDeleteReport_AdminAccess() {
        // Arrange
        setAdminSecurityContext();
        doNothing().when(reportService).deleteReport("report-123");
        
        // Act
        var response = reportController.deleteReport("report-123");
        
        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(reportService).deleteReport("report-123");
        
        clearSecurityContext();
    }
    
    @Test
    void testDeleteReport_RegularUserAccessDenied() {
        // Arrange
        setUserSecurityContext();
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reportController.deleteReport("report-123");
        });
        
        verify(reportService, never()).deleteReport(anyString());
        clearSecurityContext();
    }
}