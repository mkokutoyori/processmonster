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
 * Entity representing a process category for organizing BPMN processes.
 * Categories help organize processes by domain (e.g., "Credit", "KYC", "Transfers").
 */
@Entity
@Table(name = "process_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProcessCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique code for the category (e.g., "CREDIT", "KYC", "TRANSFER")
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Display name of the category
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the category
     */
    @Column(length = 500)
    private String description;

    /**
     * Icon name (Material icon or custom) for UI display
     */
    @Column(length = 50)
    private String icon;

    /**
     * Color hex code for UI display (e.g., "#1976d2")
     */
    @Column(length = 7)
    private String color;

    /**
     * Display order for sorting categories
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this category is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * When the category was soft deleted
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
}
