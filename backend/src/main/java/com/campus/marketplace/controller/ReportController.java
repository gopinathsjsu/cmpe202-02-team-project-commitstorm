package com.campus.marketplace.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.marketplace.dto.ReportDTO;
import com.campus.marketplace.entity.Report;
import com.campus.marketplace.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    /**
     * Create a new report.
     */
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportDTO reportDTO) {
        try {
            Report createdReport = reportService.createReport(
                reportDTO.getReporterId(),
                reportDTO.getTargetType(),
                reportDTO.getTargetId(),
                reportDTO.getReason()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new ReportDTO(createdReport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get a report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable String id) {
        return reportService.getReportById(id)
                .map(report -> ResponseEntity.ok(new ReportDTO(report)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all reports.
     * Admin only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        try {
            ensureAdminAccess();
            List<ReportDTO> reports = reportService.getAllReports().stream()
                    .map(ReportDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reports);
        } catch (AccessDeniedException e) {
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get reports by reporter ID.
     */
    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<ReportDTO>> getReportsByReporter(@PathVariable String reporterId) {
        List<ReportDTO> reports = reportService.getReportsByReporter(reporterId).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports by moderator ID.
     */
    @GetMapping("/moderator/{moderatorId}")
    public ResponseEntity<List<ReportDTO>> getReportsByModerator(@PathVariable String moderatorId) {
        List<ReportDTO> reports = reportService.getReportsByModerator(moderatorId).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable Report.ReportStatus status) {
        List<ReportDTO> reports = reportService.getReportsByStatus(status).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports by target type.
     */
    @GetMapping("/target-type/{targetType}")
    public ResponseEntity<List<ReportDTO>> getReportsByTargetType(@PathVariable Report.TargetType targetType) {
        List<ReportDTO> reports = reportService.getReportsByTargetType(targetType).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports by target ID.
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<ReportDTO>> getReportsByTargetId(@PathVariable String targetId) {
        List<ReportDTO> reports = reportService.getReportsByTargetId(targetId).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports by target type and status.
     */
    @GetMapping("/target-type/{targetType}/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByTargetTypeAndStatus(
            @PathVariable Report.TargetType targetType, 
            @PathVariable Report.ReportStatus status) {
        List<ReportDTO> reports = reportService.getReportsByTargetTypeAndStatus(targetType, status).stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Update a report.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportDTO> updateReport(@PathVariable String id, @Valid @RequestBody ReportDTO reportDTO) {
        return reportService.getReportById(id)
                .map(existingReport -> {
                    existingReport.setTargetType(reportDTO.getTargetType());
                    existingReport.setTargetId(reportDTO.getTargetId());
                    existingReport.setReason(reportDTO.getReason());
                    existingReport.setStatus(reportDTO.getStatus());
                    
                    Report updatedReport = reportService.updateReport(existingReport);
                    return ResponseEntity.ok(new ReportDTO(updatedReport));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Assign a moderator to a report.
     * Admin only.
     */
    @PatchMapping("/{id}/assign-moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportDTO> assignModerator(@PathVariable String id, @RequestParam String moderatorId) {
        ensureAdminAccess();
        try {
            Report updatedReport = reportService.assignModerator(id, moderatorId);
            return ResponseEntity.ok(new ReportDTO(updatedReport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update report status.
     * Admin only.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportDTO> updateReportStatus(@PathVariable String id, @RequestParam Report.ReportStatus status) {
        ensureAdminAccess();
        try {
            Report updatedReport = reportService.updateReportStatus(id, status);
            return ResponseEntity.ok(new ReportDTO(updatedReport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a report.
     * Admin only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        ensureAdminAccess();
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
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