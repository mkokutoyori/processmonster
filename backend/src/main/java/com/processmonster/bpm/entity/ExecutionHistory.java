package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing execution history events for a process instance
 * Immutable audit log of all activities in a process execution
 */
@Entity
@Table(name = "execution_history",
        indexes = {
            @Index(name = "idx_history_instance", columnList = "process_instance_id"),
            @Index(name = "idx_history_event_type", columnList = "eventType"),
            @Index(name = "idx_history_activity", columnList = "activityId"),
            @Index(name = "idx_history_timestamp", columnList = "timestamp")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the process instance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_instance_id", nullable = false)
    private ProcessInstance processInstance;

    /**
     * Type of event
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventType eventType;

    /**
     * Activity ID (from BPMN definition)
     */
    @Column(length = 255)
    private String activityId;

    /**
     * Activity name
     */
    @Column(length = 500)
    private String activityName;

    /**
     * Activity type (startEvent, userTask, serviceTask, endEvent, etc.)
     */
    @Column(length = 50)
    private String activityType;

    /**
     * When this event occurred
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Duration for activity execution (in milliseconds)
     * Only for ACTIVITY_COMPLETED events
     */
    private Long durationMillis;

    /**
     * User who performed the action (for user tasks)
     */
    @Column(length = 100)
    private String performedBy;

    /**
     * Event details/message
     */
    @Column(columnDefinition = "TEXT")
    private String eventDetails;

    /**
     * Error message (for error events)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Variables snapshot (JSON format)
     * Captures variable state at this point in execution
     */
    @Column(columnDefinition = "TEXT")
    private String variablesSnapshot;

    /**
     * Audit creation timestamp (immutable)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Event type enum
     */
    public enum EventType {
        // Process lifecycle
        PROCESS_STARTED,
        PROCESS_SUSPENDED,
        PROCESS_RESUMED,
        PROCESS_COMPLETED,
        PROCESS_FAILED,
        PROCESS_TERMINATED,

        // Activity lifecycle
        ACTIVITY_STARTED,
        ACTIVITY_COMPLETED,
        ACTIVITY_FAILED,
        ACTIVITY_SKIPPED,

        // Variable changes
        VARIABLE_CREATED,
        VARIABLE_UPDATED,
        VARIABLE_DELETED,

        // Tasks
        TASK_CREATED,
        TASK_ASSIGNED,
        TASK_COMPLETED,
        TASK_DELEGATED,

        // Errors
        ERROR_OCCURRED,
        ERROR_HANDLED,

        // Other
        COMMENT_ADDED,
        ATTACHMENT_ADDED,
        CUSTOM_EVENT
    }

    /**
     * Check if this is a process-level event
     */
    public boolean isProcessEvent() {
        return eventType.name().startsWith("PROCESS_");
    }

    /**
     * Check if this is an activity-level event
     */
    public boolean isActivityEvent() {
        return eventType.name().startsWith("ACTIVITY_");
    }

    /**
     * Check if this is a variable-level event
     */
    public boolean isVariableEvent() {
        return eventType.name().startsWith("VARIABLE_");
    }

    /**
     * Check if this is an error event
     */
    public boolean isErrorEvent() {
        return eventType.name().contains("ERROR") || eventType.name().contains("FAILED");
    }
}
