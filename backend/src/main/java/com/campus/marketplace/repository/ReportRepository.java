package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    
    /**
     * Find reports by reporter ID.
     */
    List<Report> findByReporterId(String reporterId);
    
    /**
     * Find reports by moderator ID.
     */
    List<Report> findByModeratorId(String moderatorId);
    
    /**
     * Find reports by status.
     */
    List<Report> findByStatus(Report.ReportStatus status);
    
    /**
     * Find reports by target type.
     */
    List<Report> findByTargetType(Report.TargetType targetType);
    
    /**
     * Find reports by target ID.
     */
    List<Report> findByTargetId(String targetId);

    /**
     * Find reports by target type and target ID.
     */
    List<Report> findByTargetTypeAndTargetId(String targetType, String targetId);
    
    /**
     * Find reports by status ordered by creation date.
     */
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(@Param("status") Report.ReportStatus status);
    
    /**
     * Find reports by target type and status ordered by creation date.
     */
    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType AND r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByTargetTypeAndStatusOrderByCreatedAtDesc(@Param("targetType") Report.TargetType targetType, @Param("status") Report.ReportStatus status);
    
    /**
     * Find reports by moderator ID ordered by creation date.
     */
    @Query("SELECT r FROM Report r WHERE r.moderator.id = :moderatorId ORDER BY r.createdAt DESC")
    List<Report> findByModeratorIdOrderByCreatedAtDesc(@Param("moderatorId") String moderatorId);
}
