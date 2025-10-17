package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReportDTO {
    
    private String id;
    private String reporterId;
    private String reporterName;
    
    @NotNull(message = "Target type is required")
    private Report.TargetType targetType;
    
    @NotBlank(message = "Target ID is required")
    private String targetId;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private Report.ReportStatus status;
    private String moderatorId;
    private String moderatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor.
     */
    public ReportDTO() {}
    
    /**
     * Constructor from Report entity.
     */
    public ReportDTO(Report report) {
        this.id = report.getId();
        this.reporterId = report.getReporter().getId();
        this.reporterName = report.getReporter().getName();
        this.targetType = report.getTargetType();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.moderatorId = report.getModerator() != null ? report.getModerator().getId() : null;
        this.moderatorName = report.getModerator() != null ? report.getModerator().getName() : null;
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getReporterId() {
        return reporterId;
    }
    
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    
    public Report.TargetType getTargetType() {
        return targetType;
    }
    
    public void setTargetType(Report.TargetType targetType) {
        this.targetType = targetType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Report.ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(Report.ReportStatus status) {
        this.status = status;
    }
    
    public String getModeratorId() {
        return moderatorId;
    }
    
    public void setModeratorId(String moderatorId) {
        this.moderatorId = moderatorId;
    }
    
    public String getModeratorName() {
        return moderatorName;
    }
    
    public void setModeratorName(String moderatorName) {
        this.moderatorName = moderatorName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
