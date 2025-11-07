package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ExecutionHistory;
import com.processmonster.bpm.entity.ExecutionHistory.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ExecutionHistory entity (immutable audit log)
 */
@Repository
public interface ExecutionHistoryRepository extends JpaRepository<ExecutionHistory, Long> {

    /**
     * Find all history for a process instance (ordered by timestamp)
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findByProcessInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Find history for a process instance (paginated)
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "ORDER BY eh.timestamp DESC")
    Page<ExecutionHistory> findByProcessInstanceIdPaged(@Param("instanceId") Long instanceId, Pageable pageable);

    /**
     * Find history by event type
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.eventType = :eventType ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findByInstanceIdAndEventType(
            @Param("instanceId") Long instanceId,
            @Param("eventType") EventType eventType);

    /**
     * Find history for specific activity
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.activityId = :activityId ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findByInstanceIdAndActivityId(
            @Param("instanceId") Long instanceId,
            @Param("activityId") String activityId);

    /**
     * Find history in date range
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.timestamp BETWEEN :startDate AND :endDate ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findByInstanceIdAndTimestampBetween(
            @Param("instanceId") Long instanceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find error events for an instance
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND (eh.eventType LIKE '%ERROR%' OR eh.eventType LIKE '%FAILED%') " +
           "ORDER BY eh.timestamp DESC")
    List<ExecutionHistory> findErrorEvents(@Param("instanceId") Long instanceId);

    /**
     * Find recent history events (last N days)
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.timestamp >= :sinceDate " +
           "ORDER BY eh.timestamp DESC")
    Page<ExecutionHistory> findRecentHistory(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);

    /**
     * Find history by performer
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.performedBy = :username " +
           "ORDER BY eh.timestamp DESC")
    Page<ExecutionHistory> findByPerformedBy(@Param("username") String username, Pageable pageable);

    /**
     * Count events by type for an instance
     */
    @Query("SELECT COUNT(eh) FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.eventType = :eventType")
    long countByInstanceIdAndEventType(
            @Param("instanceId") Long instanceId,
            @Param("eventType") EventType eventType);

    /**
     * Count total events for an instance
     */
    @Query("SELECT COUNT(eh) FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId")
    long countByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Find process lifecycle events
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.eventType IN ('PROCESS_STARTED', 'PROCESS_SUSPENDED', 'PROCESS_RESUMED', " +
           "'PROCESS_COMPLETED', 'PROCESS_FAILED', 'PROCESS_TERMINATED') " +
           "ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findProcessLifecycleEvents(@Param("instanceId") Long instanceId);

    /**
     * Find activity events for timeline
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.activityId IS NOT NULL ORDER BY eh.timestamp ASC")
    List<ExecutionHistory> findActivityEvents(@Param("instanceId") Long instanceId);

    /**
     * Find variable change events
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.processInstance.id = :instanceId " +
           "AND eh.eventType IN ('VARIABLE_CREATED', 'VARIABLE_UPDATED', 'VARIABLE_DELETED') " +
           "ORDER BY eh.timestamp DESC")
    List<ExecutionHistory> findVariableChangeEvents(@Param("instanceId") Long instanceId);

    /**
     * Get average activity duration by activity ID
     */
    @Query("SELECT AVG(eh.durationMillis) FROM ExecutionHistory eh " +
           "WHERE eh.activityId = :activityId AND eh.eventType = 'ACTIVITY_COMPLETED' " +
           "AND eh.durationMillis IS NOT NULL")
    Double getAverageActivityDuration(@Param("activityId") String activityId);

    /**
     * Find longest running activities
     */
    @Query("SELECT eh FROM ExecutionHistory eh WHERE eh.eventType = 'ACTIVITY_COMPLETED' " +
           "AND eh.durationMillis IS NOT NULL ORDER BY eh.durationMillis DESC")
    Page<ExecutionHistory> findLongestActivities(Pageable pageable);
}
