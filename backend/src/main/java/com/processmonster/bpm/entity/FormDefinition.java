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
 * Entity representing a dynamic form definition using JSON Schema
 */
@Entity
@Table(name = "form_definitions", indexes = {
        @Index(name = "idx_form_key", columnList = "formKey"),
        @Index(name = "idx_form_category", columnList = "category"),
        @Index(name = "idx_form_version", columnList = "version"),
        @Index(name = "idx_form_published", columnList = "published"),
        @Index(name = "idx_form_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique form key (e.g., "loan-application", "account-opening")
     */
    @Column(nullable = false, length = 100)
    private String formKey;

    /**
     * Form name (e.g., "Loan Application Form")
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Form description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Form category (e.g., "LOAN", "ACCOUNT", "CUSTOMER", "TRANSACTION")
     */
    @Column(length = 50)
    private String category;

    /**
     * Form version (incremented automatically)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * JSON Schema Draft 7 definition of the form
     * Contains field definitions, validation rules, conditional logic, etc.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String schemaJson;

    /**
     * UI Schema for rendering hints (optional)
     * Contains layout, widgets, styling hints for frontend
     */
    @Column(columnDefinition = "TEXT")
    private String uiSchemaJson;

    /**
     * Whether this form is published and available for use
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    /**
     * Whether this is the latest version of this form key
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isLatestVersion = true;

    /**
     * Associated process definition key (optional)
     * Links this form to a specific process
     */
    @Column(length = 100)
    private String processDefinitionKey;

    /**
     * Associated task definition key (optional)
     * Links this form to a specific task type
     */
    @Column(length = 100)
    private String taskDefinitionKey;

    /**
     * Form submissions using this definition
     */
    @OneToMany(mappedBy = "formDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FormSubmission> submissions = new ArrayList<>();

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
     * Add a submission to this form definition
     */
    public void addSubmission(FormSubmission submission) {
        submissions.add(submission);
        submission.setFormDefinition(this);
    }

    /**
     * Remove a submission from this form definition
     */
    public void removeSubmission(FormSubmission submission) {
        submissions.remove(submission);
        submission.setFormDefinition(null);
    }

    /**
     * Mark as deleted (soft delete)
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Check if this form is active (published and not deleted)
     */
    public boolean isActive() {
        return published && !deleted;
    }
}
