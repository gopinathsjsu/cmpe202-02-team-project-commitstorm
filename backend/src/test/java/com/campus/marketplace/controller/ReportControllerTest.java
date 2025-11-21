package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Report;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.ReportService;
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
public class ReportControllerTest {
    
    @Mock
    private ReportService reportService;
    
    @InjectMocks
    private ReportController reportController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Report testReport;
    private User reporter;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        objectMapper = new ObjectMapper();
        
        reporter = new User();
        reporter.setId("reporter-123");
        reporter.setName("Reporter");
        
        testReport = new Report();
        testReport.setId("report-123");
        testReport.setReporter(reporter);
        testReport.setReason("Inappropriate content");
        testReport.setStatus(Report.ReportStatus.OPEN);
        testReport.setTargetType(Report.TargetType.LISTING);
        testReport.setTargetId("listing-123");
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
    void testCreateReport() throws Exception {
        when(reportService.createReport(any(), any(), any(), any()))
            .thenReturn(testReport);
        
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("Inappropriate content"));
    }
    
    @Test
    void testGetReportById_Found() throws Exception {
        when(reportService.getReportById("report-123")).thenReturn(Optional.of(testReport));
        
        mockMvc.perform(get("/api/reports/report-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("report-123"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }
    
    @Test
    void testGetReportById_NotFound() throws Exception {
        when(reportService.getReportById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/reports/non-existent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetAllReports() throws Exception {
        List<Report> reports = Arrays.asList(testReport);
        when(reportService.getAllReports()).thenReturn(reports);
        setAdminAuthentication();
        
        mockMvc.perform(get("/api/reports")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reason").value("Inappropriate content"));
    }
    
    @Test
    void testGetReportsByStatus() throws Exception {
        List<Report> reports = Arrays.asList(testReport);
        when(reportService.getReportsByStatus(Report.ReportStatus.OPEN)).thenReturn(reports);
        
        mockMvc.perform(get("/api/reports/status/OPEN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("OPEN"));
    }
    
    @Test
    void testUpdateReportStatus() throws Exception {
        testReport.setStatus(Report.ReportStatus.RESOLVED);
        when(reportService.updateReportStatus(any(), any())).thenReturn(testReport);
        setAdminAuthentication();
        
        mockMvc.perform(patch("/api/reports/report-123/status?status=RESOLVED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
    
    @Test
    void testDeleteReport() throws Exception {
        setAdminAuthentication();
        mockMvc.perform(delete("/api/reports/report-123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        
        verify(reportService, times(1)).deleteReport("report-123");
    }
}
