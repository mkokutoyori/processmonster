package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.ProcessInstance.ProcessInstanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessInstance entity
 */
@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, Long> {

    /**
     * Find all process instances (not deleted)
     */
    Page<ProcessInstance> findByDeletedFalse(Pageable pageable);

    /**
     * Find instance by ID and not deleted
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.id = :id AND pi.deleted = false")
    Optional<ProcessInstance> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Find instances by status
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.status = :status AND pi.deleted = false")
    Page<ProcessInstance> findByStatus(@Param("status") ProcessInstanceStatus status, Pageable pageable);

    /**
     * Find active instances (RUNNING or SUSPENDED)
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.status IN ('RUNNING', 'SUSPENDED') AND pi.deleted = false")
    Page<ProcessInstance> findActiveInstances(Pageable pageable);

    /**
     * Find instances by process definition
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.processDefinition.id = :processDefinitionId AND pi.deleted = false")
    Page<ProcessInstance> findByProcessDefinitionId(@Param("processDefinitionId") Long processDefinitionId, Pageable pageable);

    /**
     * Find instances by process key
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.processDefinition.processKey = :processKey AND pi.deleted = false")
    Page<ProcessInstance> findByProcessKey(@Param("processKey") String processKey, Pageable pageable);

    /**
     * Find instance by business key
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.businessKey = :businessKey AND pi.deleted = false")
    Optional<ProcessInstance> findByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * Find instances started by user
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.startedBy = :username AND pi.deleted = false")
    Page<ProcessInstance> findByStartedBy(@Param("username") String username, Pageable pageable);

    /**
     * Find instances started in date range
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.startTime BETWEEN :startDate AND :endDate AND pi.deleted = false")
    Page<ProcessInstance> findByStartTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find child instances of a parent
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.parentInstanceId = :parentId AND pi.deleted = false")
    List<ProcessInstance> findChildInstances(@Param("parentId") Long parentId);

    /**
     * Count instances by status
     */
    @Query("SELECT COUNT(pi) FROM ProcessInstance pi WHERE pi.status = :status AND pi.deleted = false")
    long countByStatus(@Param("status") ProcessInstanceStatus status);

    /**
     * Count active instances for a process definition
     */
    @Query("SELECT COUNT(pi) FROM ProcessInstance pi WHERE pi.processDefinition.id = :processDefinitionId " +
           "AND pi.status IN ('RUNNING', 'SUSPENDED') AND pi.deleted = false")
    long countActiveInstancesByProcessDefinition(@Param("processDefinitionId") Long processDefinitionId);

    /**
     * Find instances by engine instance ID
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.engineInstanceId = :engineInstanceId AND pi.deleted = false")
    Optional<ProcessInstance> findByEngineInstanceId(@Param("engineInstanceId") String engineInstanceId);

    /**
     * Search instances by business key or started by
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.deleted = false AND " +
           "(LOWER(pi.businessKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pi.startedBy) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProcessInstance> searchInstances(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find instances that have been running longer than threshold
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.status = 'RUNNING' " +
           "AND pi.startTime < :thresholdTime AND pi.deleted = false")
    List<ProcessInstance> findLongRunningInstances(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Find failed instances that haven't been handled
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.status = 'FAILED' " +
           "AND pi.deleted = false ORDER BY pi.endTime DESC")
    Page<ProcessInstance> findFailedInstances(Pageable pageable);

    /**
     * Find all instances (not deleted) - List version
     */
    List<ProcessInstance> findByDeletedFalse();

    /**
     * Count all instances (not deleted)
     */
    long countByDeletedFalse();

    /**
     * Count instances by status (not deleted)
     */
    long countByStatusAndDeletedFalse(ProcessInstanceStatus status);

    /**
     * Count instances by status with end time after given date
     */
    @Query("SELECT COUNT(pi) FROM ProcessInstance pi WHERE pi.status = :status " +
           "AND pi.endTime > :endTime AND pi.deleted = false")
    long countByStatusAndEndTimeAfterAndDeletedFalse(
            @Param("status") ProcessInstanceStatus status,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find instances by process definition key (not deleted)
     */
    @Query("SELECT pi FROM ProcessInstance pi WHERE pi.processDefinition.processKey = :processKey AND pi.deleted = false")
    List<ProcessInstance> findByProcessDefinitionKeyAndDeletedFalse(@Param("processKey") String processKey);

    /**
     * Find instances by status (not deleted) - List version
     */
    List<ProcessInstance> findByStatusAndDeletedFalse(ProcessInstanceStatus status);
}
