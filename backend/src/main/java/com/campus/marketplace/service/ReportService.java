package com.campus.marketplace.service;

import com.campus.marketplace.entity.Report;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ReportRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReportService {
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new report with specified parameters.
     */
    public Report createReport(String reporterId, Report.TargetType targetType, String targetId, String reason) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found with id: " + reporterId));
        
        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setStatus(Report.ReportStatus.OPEN);
        
        return reportRepository.save(report);
    }
    
    /**
     * Create a new report from entity.
     */
    public Report createReport(Report report) {
        if (report.getId() == null) {
            report.setId(UUID.randomUUID().toString());
        }
        return reportRepository.save(report);
    }
    
    /**
     * Get a report by ID.
     */
    public Optional<Report> getReportById(String id) {
        return reportRepository.findById(id);
    }
    
    /**
     * Get all reports.
     */
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
    
    /**
     * Get reports by reporter ID.
     */
    public List<Report> getReportsByReporter(String reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }
    
    /**
     * Get reports by moderator ID.
     */
    public List<Report> getReportsByModerator(String moderatorId) {
        return reportRepository.findByModeratorId(moderatorId);
    }
    
    /**
     * Get reports by status.
     */
    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Get reports by target type.
     */
    public List<Report> getReportsByTargetType(Report.TargetType targetType) {
        return reportRepository.findByTargetType(targetType);
    }
    
    /**
     * Get reports by target ID.
     */
    public List<Report> getReportsByTargetId(String targetId) {
        return reportRepository.findByTargetId(targetId);
    }
    
    /**
     * Get reports by target type and status.
     */
    public List<Report> getReportsByTargetTypeAndStatus(Report.TargetType targetType, Report.ReportStatus status) {
        return reportRepository.findByTargetTypeAndStatusOrderByCreatedAtDesc(targetType, status);
    }
    
    /**
     * Update a report.
     */
    public Report updateReport(Report report) {
        return reportRepository.save(report);
    }
    
    /**
     * Assign a moderator to a report.
     */
    public Report assignModerator(String reportId, String moderatorId) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        Optional<User> moderatorOpt = userRepository.findById(moderatorId);
        
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Report not found with id: " + reportId);
        }
        if (moderatorOpt.isEmpty()) {
            throw new RuntimeException("Moderator not found with id: " + moderatorId);
        }
        
        Report report = reportOpt.get();
        report.setModerator(moderatorOpt.get());
        report.setStatus(Report.ReportStatus.IN_REVIEW);
        
        return reportRepository.save(report);
    }
    
    /**
     * Update report status.
     */
    public Report updateReportStatus(String reportId, Report.ReportStatus status) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Report not found with id: " + reportId);
        }
        
        Report report = reportOpt.get();
        report.setStatus(status);
        
        return reportRepository.save(report);
    }
    
    /**
     * Delete a report.
     */
    public void deleteReport(String id) {
        reportRepository.deleteById(id);
    }
}
