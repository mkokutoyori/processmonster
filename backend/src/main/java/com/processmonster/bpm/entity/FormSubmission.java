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

/**
 * Entity representing a form submission with data
 */
@Entity
@Table(name = "form_submissions", indexes = {
        @Index(name = "idx_submission_form_def", columnList = "form_definition_id"),
        @Index(name = "idx_submission_task", columnList = "task_id"),
        @Index(name = "idx_submission_process", columnList = "process_instance_id"),
        @Index(name = "idx_submission_status", columnList = "status"),
        @Index(name = "idx_submission_submitted_by", columnList = "submittedBy"),
        @Index(name = "idx_submission_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Form definition used for this submission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_definition_id", nullable = false)
    private FormDefinition formDefinition;

    /**
     * Associated task (if form was submitted as part of a task)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    /**
     * Associated process instance (if form was submitted as part of a process)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_instance_id")
    private ProcessInstance processInstance;

    /**
     * JSON data submitted by the user
     * Conforms to the JSON Schema in FormDefinition
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String dataJson;

    /**
     * Submission status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    /**
     * User who submitted the form
     */
    @Column(nullable = false, length = 100)
    private String submittedBy;

    /**
     * When the form was submitted (null if still draft)
     */
    @Column
    private LocalDateTime submittedAt;

    /**
     * Validation errors (if any)
     */
    @Column(columnDefinition = "TEXT")
    private String validationErrors;

    /**
     * Business key for tracking (optional)
     */
    @Column(length = 200)
    private String businessKey;

    /**
     * Notes or comments about this submission
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

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

    @Column
    private LocalDateTime deletedAt;

    /**
     * Mark as deleted (soft delete)
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Submit the form (change status from DRAFT to SUBMITTED)
     */
    public void submit() {
        if (this.status == SubmissionStatus.DRAFT) {
            this.status = SubmissionStatus.SUBMITTED;
            this.submittedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if this submission is a draft
     */
    public boolean isDraft() {
        return status == SubmissionStatus.DRAFT;
    }

    /**
     * Check if this submission is submitted
     */
    public boolean isSubmitted() {
        return status == SubmissionStatus.SUBMITTED || status == SubmissionStatus.APPROVED;
    }
}
