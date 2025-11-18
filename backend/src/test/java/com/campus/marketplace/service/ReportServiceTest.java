package com.campus.marketplace.service;

import com.campus.marketplace.entity.Report;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ReportRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    
    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ReportService reportService;
    
    private Report testReport;
    private User reporter;
    
    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId("reporter-123");
        reporter.setName("Reporter");
        reporter.setEmail("reporter@example.com");
        
        testReport = new Report();
        testReport.setId("report-123");
        testReport.setReporter(reporter);
        testReport.setReason("Inappropriate content");
        testReport.setStatus(Report.ReportStatus.OPEN);
        testReport.setTargetType(Report.TargetType.LISTING);
        testReport.setTargetId("listing-123");
    }
    
    @Test
    void testCreateReport_Success() {
        when(userRepository.findById("reporter-123")).thenReturn(Optional.of(reporter));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);
        
        Report result = reportService.createReport(
            "reporter-123", Report.TargetType.LISTING, "listing-123", "Inappropriate"
        );
        
        assertNotNull(result);
        assertEquals(Report.ReportStatus.OPEN, result.getStatus());
        verify(reportRepository, times(1)).save(any(Report.class));
    }
    
    @Test
    void testCreateReport_ReporterNotFound() {
        when(userRepository.findById("reporter-999")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            reportService.createReport("reporter-999", Report.TargetType.LISTING, "listing-123", "Bad")
        );
        
        verify(reportRepository, never()).save(any(Report.class));
    }
    
    @Test
    void testGetReportById_Found() {
        when(reportRepository.findById("report-123")).thenReturn(Optional.of(testReport));
        
        Optional<Report> result = reportService.getReportById("report-123");
        
        assertTrue(result.isPresent());
        assertEquals("report-123", result.get().getId());
    }
    
    @Test
    void testGetReportById_NotFound() {
        when(reportRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        Optional<Report> result = reportService.getReportById("non-existent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetAllReports() {
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findAll()).thenReturn(reports);
        
        List<Report> result = reportService.getAllReports();
        
        assertEquals(1, result.size());
        verify(reportRepository, times(1)).findAll();
    }
    
    @Test
    void testGetReportsByStatus() {
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.OPEN))
            .thenReturn(reports);
        
        List<Report> result = reportService.getReportsByStatus(Report.ReportStatus.OPEN);
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetReportsByReporter() {
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findByReporterId("reporter-123")).thenReturn(reports);
        
        List<Report> result = reportService.getReportsByReporter("reporter-123");
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testUpdateReportStatus() {
        testReport.setStatus(Report.ReportStatus.RESOLVED);
        when(reportRepository.findById("report-123")).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);
        
        Report result = reportService.updateReportStatus("report-123", Report.ReportStatus.RESOLVED);
        
        assertEquals(Report.ReportStatus.RESOLVED, result.getStatus());
    }
    
    @Test
    void testUpdateReportStatus_NotFound() {
        when(reportRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            reportService.updateReportStatus("non-existent", Report.ReportStatus.RESOLVED)
        );
    }
    
    @Test
    void testDeleteReport() {
        reportService.deleteReport("report-123");
        
        verify(reportRepository, times(1)).deleteById("report-123");
    }
}
