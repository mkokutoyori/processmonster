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
 * Entity representing a BPMN process definition with versioning support.
 * Each unique combination of key + version represents a specific process version.
 */
@Entity
@Table(name = "process_definitions",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"processKey", "version"})
        },
        indexes = {
            @Index(name = "idx_process_key", columnList = "processKey"),
            @Index(name = "idx_process_category", columnList = "category_id"),
            @Index(name = "idx_process_latest", columnList = "isLatestVersion")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProcessDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique process key (e.g., "credit-approval", "kyc-verification")
     * Combined with version to identify a specific process version
     */
    @Column(nullable = false, length = 100)
    private String processKey;

    /**
     * Process display name
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Version number (auto-incremented for each process key)
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * Whether this is the latest version of the process
     * Used for quick lookups of the current active version
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isLatestVersion = true;

    /**
     * BPMN 2.0 XML content
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bpmnXml;

    /**
     * Process description
     */
    @Column(length = 1000)
    private String description;

    /**
     * Category this process belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProcessCategory category;

    /**
     * Whether this is a template that can be used to create new processes
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTemplate = false;

    /**
     * Whether this process is published and ready for deployment
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    /**
     * Whether this process is active and can be instantiated
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Tags for searching and filtering (comma-separated)
     */
    @Column(length = 500)
    private String tags;

    /**
     * Deployment ID in the BPMN engine (Camunda/Flowable)
     * Null if not yet deployed
     */
    @Column(length = 255)
    private String deploymentId;

    /**
     * When the process was deployed to the engine
     */
    private LocalDateTime deployedAt;

    /**
     * Who deployed the process
     */
    @Column(length = 100)
    private String deployedBy;

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * When the process was soft deleted
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
     * Helper method to check if process is deployed
     */
    public boolean isDeployed() {
        return deploymentId != null && deployedAt != null;
    }

    /**
     * Helper method to get full process identifier
     */
    public String getFullIdentifier() {
        return processKey + ":v" + version;
    }
}
