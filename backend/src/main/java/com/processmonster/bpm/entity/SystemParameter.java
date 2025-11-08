package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * System configuration parameter entity.
 * Stores application settings that can be modified at runtime.
 */
@Entity
@Table(name = "system_parameters", indexes = {
        @Index(name = "idx_param_key", columnList = "paramKey", unique = true),
        @Index(name = "idx_param_category", columnList = "category"),
        @Index(name = "idx_param_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parameter key (unique identifier)
     */
    @Column(nullable = false, unique = true, length = 100, name = "paramKey")
    private String key;

    /**
     * Parameter value
     */
    @Column(columnDefinition = "TEXT")
    private String value;

    /**
     * Parameter description
     */
    @Column(length = 500)
    private String description;

    /**
     * Parameter category (SYSTEM, SECURITY, NOTIFICATION, INTEGRATION, etc.)
     */
    @Column(length = 50)
    private String category;

    /**
     * Data type (STRING, INTEGER, BOOLEAN, JSON, etc.)
     */
    @Column(length = 20)
    @Builder.Default
    private String dataType = "STRING";

    /**
     * Default value
     */
    @Column(length = 1000)
    private String defaultValue;

    /**
     * Whether the parameter is encrypted (for sensitive values)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean encrypted = false;

    /**
     * Whether the parameter is editable via UI
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean editable = true;

    /**
     * Validation regex pattern (optional)
     */
    @Column(length = 500)
    private String validationPattern;

    /**
     * Allowed values (comma-separated for enum-like parameters)
     */
    @Column(length = 1000)
    private String allowedValues;

    /**
     * Display order for UI
     */
    @Column
    private Integer displayOrder;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String updatedBy;

    // Soft delete
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system";
        }
        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get value as boolean
     */
    public Boolean getBooleanValue() {
        if (value == null) {
            return defaultValue != null ? Boolean.parseBoolean(defaultValue) : false;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Get value as integer
     */
    public Integer getIntegerValue() {
        if (value == null) {
            return defaultValue != null ? Integer.parseInt(defaultValue) : 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get value as long
     */
    public Long getLongValue() {
        if (value == null) {
            return defaultValue != null ? Long.parseLong(defaultValue) : 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Get value or default
     */
    public String getValueOrDefault() {
        return value != null ? value : defaultValue;
    }
}
