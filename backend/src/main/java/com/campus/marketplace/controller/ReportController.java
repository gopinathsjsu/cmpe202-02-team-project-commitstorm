package com.campus.marketplace.controller;

import com.campus.marketplace.dto.ReportDTO;
import com.campus.marketplace.entity.Report;
import com.campus.marketplace.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     */
    @GetMapping
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<ReportDTO> reports = reportService.getAllReports().stream()
                .map(ReportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
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
     */
    @PatchMapping("/{id}/assign-moderator")
    public ResponseEntity<ReportDTO> assignModerator(@PathVariable String id, @RequestParam String moderatorId) {
        try {
            Report updatedReport = reportService.assignModerator(id, moderatorId);
            return ResponseEntity.ok(new ReportDTO(updatedReport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update report status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDTO> updateReportStatus(@PathVariable String id, @RequestParam Report.ReportStatus status) {
        try {
            Report updatedReport = reportService.updateReportStatus(id, status);
            return ResponseEntity.ok(new ReportDTO(updatedReport));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a report.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
