package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity (read-only, append-only)
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs (paginated)
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find logs by username
     */
    @Query("SELECT al FROM AuditLog al WHERE al.username = :username ORDER BY al.timestamp DESC")
    Page<AuditLog> findByUsername(@Param("username") String username, Pageable pageable);

    /**
     * Find logs by action
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action ORDER BY al.timestamp DESC")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    /**
     * Find logs by entity type and ID
     */
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
           "AND al.entityId = :entityId ORDER BY al.timestamp DESC")
    Page<AuditLog> findByEntityTypeAndEntityId(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            Pageable pageable);

    /**
     * Find logs by date range
     */
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find logs by IP address
     */
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress ORDER BY al.timestamp DESC")
    Page<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);

    /**
     * Find security-related logs
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action LIKE 'LOGIN%' " +
           "OR al.action LIKE 'LOGOUT%' OR al.action LIKE '%AUTH%' " +
           "OR al.action LIKE '%PERMISSION%' OR al.action LIKE '%ROLE%' " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findSecurityLogs(Pageable pageable);

    /**
     * Find failed actions
     */
    @Query("SELECT al FROM AuditLog al WHERE al.result = 'FAILURE' OR al.result = 'ERROR' " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findFailedActions(Pageable pageable);

    /**
     * Find logs by severity
     */
    @Query("SELECT al FROM AuditLog al WHERE al.severity = :severity ORDER BY al.timestamp DESC")
    Page<AuditLog> findBySeverity(@Param("severity") String severity, Pageable pageable);

    /**
     * Search logs (username, action, entity type, or entity name)
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "LOWER(al.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(al.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(al.entityType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(al.entityName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> searchLogs(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count logs by username
     */
    long countByUsername(String username);

    /**
     * Count logs by action
     */
    long countByAction(String action);

    /**
     * Count failed login attempts for a user in time range
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.username = :username " +
           "AND al.action = 'LOGIN_FAILED' AND al.timestamp > :since")
    long countFailedLoginsSince(@Param("username") String username, @Param("since") LocalDateTime since);

    /**
     * Find recent logs for a user
     */
    @Query("SELECT al FROM AuditLog al WHERE al.username = :username " +
           "AND al.timestamp > :since ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentLogsByUsername(@Param("username") String username, @Param("since") LocalDateTime since);

    /**
     * Delete old logs (cleanup for compliance - only after retention period)
     */
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count logs in date range
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate")
    long countByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
