package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a user task in a process instance
 */
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_assignee", columnList = "assignee"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_process_instance", columnList = "process_instance_id"),
        @Index(name = "idx_task_due_date", columnList = "dueDate"),
        @Index(name = "idx_task_priority", columnList = "priority"),
        @Index(name = "idx_task_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Task name
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Task description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Task status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.CREATED;

    /**
     * Task priority
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.NORMAL;

    /**
     * Current assignee username
     */
    @Column(length = 100)
    private String assignee;

    /**
     * Candidate group (for queue tasks)
     */
    @Column(length = 100)
    private String candidateGroup;

    /**
     * Due date for task completion
     */
    private LocalDateTime dueDate;

    /**
     * Follow-up date for reminders
     */
    private LocalDateTime followUpDate;

    /**
     * Associated process instance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_instance_id")
    private ProcessInstance processInstance;

    /**
     * Activity ID in BPMN diagram
     */
    @Column(length = 100)
    private String activityId;

    /**
     * Form key for rendering task form
     */
    @Column(length = 200)
    private String formKey;

    /**
     * Camunda task ID for synchronization
     */
    @Column(length = 100, unique = true)
    private String camundaTaskId;

    /**
     * Process instance ID from Camunda
     */
    @Column(length = 100)
    private String processInstanceId;

    /**
     * Completion date
     */
    private LocalDateTime completedDate;

    /**
     * User who completed the task
     */
    @Column(length = 100)
    private String completedBy;

    /**
     * Task comments
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();

    /**
     * Task attachments
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    /**
     * Claim date (when task was claimed by assignee)
     */
    private LocalDateTime claimedDate;

    /**
     * User who claimed the task
     */
    @Column(length = 100)
    private String claimedBy;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(nullable = false, length = 100)
    private String updatedBy;

    // Soft delete
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    /**
     * Task status enum
     */
    public enum TaskStatus {
        CREATED,      // Task created but not yet claimed
        ASSIGNED,     // Task assigned to a user
        IN_PROGRESS,  // Task is being worked on
        COMPLETED,    // Task completed successfully
        CANCELLED     // Task cancelled
    }

    /**
     * Task priority enum
     */
    public enum TaskPriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }

    /**
     * Check if task is active (not completed or cancelled)
     */
    public boolean isActive() {
        return status != TaskStatus.COMPLETED && status != TaskStatus.CANCELLED;
    }

    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        return dueDate != null &&
               LocalDateTime.now().isAfter(dueDate) &&
               isActive();
    }

    /**
     * Add comment to task
     */
    public void addComment(TaskComment comment) {
        comments.add(comment);
        comment.setTask(this);
    }

    /**
     * Add attachment to task
     */
    public void addAttachment(TaskAttachment attachment) {
        attachments.add(attachment);
        attachment.setTask(this);
    }
}
