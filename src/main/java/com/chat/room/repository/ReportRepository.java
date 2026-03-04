package com.chat.room.repository;

import com.chat.room.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);

    Page<Report> findByReporterId(Long reporterId, Pageable pageable);

    Page<Report> findByReportedUserId(Long reportedUserId, Pageable pageable);

    Page<Report> findByReportedRoomId(Long reportedRoomId, Pageable pageable);

    Page<Report> findByType(Report.ReportType type, Pageable pageable);

    Page<Report> findByTargetType(Report.TargetType targetType, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = :status AND r.createdAt BETWEEN :start AND :end")
    List<Report> findByStatusAndDateRange(Report.ReportStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reporterId = :reporterId AND r.createdAt >= :since")
    long countByReporterIdSince(Long reporterId, LocalDateTime since);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportedUserId = :userId AND r.status = 'PENDING'")
    long countPendingReportsByUserId(Long userId);

    boolean existsByReporterIdAndReportedUserIdAndStatus(Long reporterId, Long reportedUserId, Report.ReportStatus status);

    boolean existsByReporterIdAndReportedMessageIdAndStatus(Long reporterId, Long reportedMessageId, Report.ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt >= :since")
    long countReportsSince(LocalDateTime since);

    @Query("SELECT r.type, COUNT(r) FROM Report r GROUP BY r.type")
    List<Object[]> countByType();

    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    List<Object[]> countByStatus();
}
