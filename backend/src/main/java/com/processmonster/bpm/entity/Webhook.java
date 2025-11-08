package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a webhook configuration for event notifications.
 * Webhooks are triggered on specific events (e.g., process.started, task.completed).
 */
@Entity
@Table(name = "webhooks", indexes = {
        @Index(name = "idx_webhook_enabled", columnList = "enabled"),
        @Index(name = "idx_webhook_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Friendly name for the webhook
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Target URL to send webhook events to
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * Description of what this webhook is used for
     */
    @Column(length = 500)
    private String description;

    /**
     * Whether the webhook is enabled
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Events that trigger this webhook
     * (e.g., process.started, process.completed, task.assigned, task.completed, form.submitted)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event")
    @Builder.Default
    private Set<String> events = new HashSet<>();

    /**
     * Secret for HMAC-SHA256 signature verification
     */
    @Column(length = 100)
    private String secret;

    /**
     * Custom headers to send with webhook requests (JSON format)
     */
    @Column(length = 2000)
    private String customHeaders;

    /**
     * HTTP method (default: POST)
     */
    @Column(length = 10)
    @Builder.Default
    private String httpMethod = "POST";

    /**
     * Content type (default: application/json)
     */
    @Column(length = 100)
    @Builder.Default
    private String contentType = "application/json";

    /**
     * Timeout in milliseconds (default: 30000 = 30s)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutMs = 30000;

    /**
     * Max retry attempts on failure (default: 3)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Retry delay in milliseconds (exponential backoff: delay * 2^attempt)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryDelayMs = 1000;

    /**
     * Total number of successful deliveries
     */
    @Column(nullable = false)
    @Builder.Default
    private Long successCount = 0L;

    /**
     * Total number of failed deliveries
     */
    @Column(nullable = false)
    @Builder.Default
    private Long failureCount = 0L;

    /**
     * Last successful delivery timestamp
     */
    private LocalDateTime lastSuccessAt;

    /**
     * Last failed delivery timestamp
     */
    private LocalDateTime lastFailureAt;

    /**
     * Last failure error message
     */
    @Column(length = 1000)
    private String lastError;

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
     * Check if the webhook is active
     */
    public boolean isActive() {
        return enabled && !deleted;
    }

    /**
     * Increment success count and update timestamp
     */
    public void recordSuccess() {
        this.successCount++;
        this.lastSuccessAt = LocalDateTime.now();
        this.lastError = null;
    }

    /**
     * Increment failure count and update timestamp
     */
    public void recordFailure(String errorMessage) {
        this.failureCount++;
        this.lastFailureAt = LocalDateTime.now();
        this.lastError = errorMessage != null && errorMessage.length() > 1000
                ? errorMessage.substring(0, 1000)
                : errorMessage;
    }

    /**
     * Check if this webhook should be triggered for a given event
     */
    public boolean shouldTriggerFor(String eventType) {
        return isActive() && events.contains(eventType);
    }
}
