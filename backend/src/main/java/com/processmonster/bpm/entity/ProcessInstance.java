package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a process instance (execution of a process definition)
 */
@Entity
@Table(name = "process_instances",
        indexes = {
            @Index(name = "idx_instance_status", columnList = "status"),
            @Index(name = "idx_instance_process_def", columnList = "process_definition_id"),
            @Index(name = "idx_instance_business_key", columnList = "businessKey"),
            @Index(name = "idx_instance_started_by", columnList = "startedBy")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProcessInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the process definition
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_definition_id", nullable = false)
    private ProcessDefinition processDefinition;

    /**
     * Business key for this instance (optional, for business correlation)
     */
    @Column(length = 255)
    private String businessKey;

    /**
     * Current status of the instance
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessInstanceStatus status;

    /**
     * When the instance was started
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * When the instance ended (completed, failed, or terminated)
     */
    private LocalDateTime endTime;

    /**
     * Duration in milliseconds (calculated when ended)
     */
    private Long durationMillis;

    /**
     * Who started this instance
     */
    @Column(nullable = false, length = 100)
    private String startedBy;

    /**
     * Current activity ID (where the process is currently at)
     */
    @Column(length = 255)
    private String currentActivityId;

    /**
     * Current activity name
     */
    @Column(length = 500)
    private String currentActivityName;

    /**
     * Error message if status is FAILED
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Stack trace if status is FAILED
     */
    @Column(columnDefinition = "TEXT")
    private String errorStackTrace;

    /**
     * Reason for suspension (if status is SUSPENDED)
     */
    @Column(length = 1000)
    private String suspensionReason;

    /**
     * Reason for termination (if status is TERMINATED)
     */
    @Column(length = 1000)
    private String terminationReason;

    /**
     * Parent process instance ID (for sub-processes)
     */
    @Column(name = "parent_instance_id")
    private Long parentInstanceId;

    /**
     * Root process instance ID (for nested sub-processes)
     */
    @Column(name = "root_instance_id")
    private Long rootInstanceId;

    /**
     * Camunda/Flowable engine instance ID (if using external engine)
     */
    @Column(length = 255)
    private String engineInstanceId;

    /**
     * Additional metadata as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * When the instance was soft deleted
     */
    private LocalDateTime deletedAt;

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    /**
     * Process instance status enum
     */
    public enum ProcessInstanceStatus {
        RUNNING,      // Process is currently executing
        SUSPENDED,    // Process is paused
        COMPLETED,    // Process finished successfully
        FAILED,       // Process ended with error
        TERMINATED    // Process was manually terminated
    }

    /**
     * Check if instance is active (running or suspended)
     */
    public boolean isActive() {
        return status == ProcessInstanceStatus.RUNNING || status == ProcessInstanceStatus.SUSPENDED;
    }

    /**
     * Check if instance is ended (completed, failed, or terminated)
     */
    public boolean isEnded() {
        return status == ProcessInstanceStatus.COMPLETED ||
               status == ProcessInstanceStatus.FAILED ||
               status == ProcessInstanceStatus.TERMINATED;
    }

    /**
     * Calculate and set duration when instance ends
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMillis = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
