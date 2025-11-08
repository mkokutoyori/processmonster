package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit log entity for tracking all sensitive operations.
 * Logs are never updated or deleted - append-only for compliance.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "username"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_ip", columnList = "ipAddress")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp of the action (immutable)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * Username who performed the action
     */
    @Column(nullable = false, length = 100, updatable = false)
    private String username;

    /**
     * Action performed (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)
     */
    @Column(nullable = false, length = 50, updatable = false)
    private String action;

    /**
     * Entity type affected (User, Process, Task, etc.)
     */
    @Column(length = 100, updatable = false)
    private String entityType;

    /**
     * Entity ID affected
     */
    @Column(updatable = false)
    private Long entityId;

    /**
     * Entity name or description
     */
    @Column(length = 500, updatable = false)
    private String entityName;

    /**
     * Previous value (JSON format) - for UPDATE/DELETE actions
     */
    @Column(columnDefinition = "TEXT", updatable = false)
    private String oldValue;

    /**
     * New value (JSON format) - for CREATE/UPDATE actions
     */
    @Column(columnDefinition = "TEXT", updatable = false)
    private String newValue;

    /**
     * HTTP method (GET, POST, PUT, DELETE)
     */
    @Column(length = 10, updatable = false)
    private String httpMethod;

    /**
     * Request URL
     */
    @Column(length = 500, updatable = false)
    private String requestUrl;

    /**
     * IP address of the client
     */
    @Column(length = 50, updatable = false)
    private String ipAddress;

    /**
     * User agent (browser/client info)
     */
    @Column(length = 500, updatable = false)
    private String userAgent;

    /**
     * Session ID
     */
    @Column(length = 100, updatable = false)
    private String sessionId;

    /**
     * Result of the action (SUCCESS, FAILURE, ERROR)
     */
    @Column(length = 20, updatable = false)
    @Builder.Default
    private String result = "SUCCESS";

    /**
     * Error message if action failed
     */
    @Column(length = 2000, updatable = false)
    private String errorMessage;

    /**
     * Additional context (JSON format)
     */
    @Column(columnDefinition = "TEXT", updatable = false)
    private String context;

    /**
     * Severity level (INFO, WARNING, ERROR, CRITICAL)
     */
    @Column(length = 20, updatable = false)
    @Builder.Default
    private String severity = "INFO";

    /**
     * Tags for categorization (comma-separated)
     */
    @Column(length = 500, updatable = false)
    private String tags;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Check if this is a sensitive action
     */
    public boolean isSensitive() {
        if (action == null) {
            return false;
        }
        return action.contains("DELETE") ||
               action.contains("UPDATE") ||
               action.equals("LOGIN_FAILED") ||
               action.contains("PASSWORD") ||
               action.contains("PERMISSION");
    }

    /**
     * Check if this is a security-related action
     */
    public boolean isSecurityRelated() {
        if (action == null) {
            return false;
        }
        return action.startsWith("LOGIN") ||
               action.startsWith("LOGOUT") ||
               action.contains("AUTH") ||
               action.contains("PERMISSION") ||
               action.contains("ROLE");
    }
}
