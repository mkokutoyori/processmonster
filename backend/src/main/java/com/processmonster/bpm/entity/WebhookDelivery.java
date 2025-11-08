package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a webhook delivery attempt.
 * Tracks individual webhook calls for audit and debugging purposes.
 */
@Entity
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "idx_webhook_delivery_webhook", columnList = "webhook_id"),
        @Index(name = "idx_webhook_delivery_status", columnList = "status"),
        @Index(name = "idx_webhook_delivery_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the webhook configuration
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;

    /**
     * Event type that triggered this delivery
     */
    @Column(nullable = false, length = 100)
    private String eventType;

    /**
     * Request payload (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String requestPayload;

    /**
     * Request headers (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * HTTP status code received
     */
    private Integer httpStatusCode;

    /**
     * Response body received
     */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /**
     * Response headers received
     */
    @Column(columnDefinition = "TEXT")
    private String responseHeaders;

    /**
     * Delivery status: PENDING, SUCCESS, FAILED, RETRYING
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * Number of retry attempts made
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Error message if delivery failed
     */
    @Column(length = 2000)
    private String errorMessage;

    /**
     * Duration of the request in milliseconds
     */
    private Long durationMs;

    /**
     * Timestamp when the delivery was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the delivery was completed (success or final failure)
     */
    private LocalDateTime completedAt;

    /**
     * Next retry attempt timestamp (for RETRYING status)
     */
    private LocalDateTime nextRetryAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Mark delivery as successful
     */
    public void markSuccess(int statusCode, String responseBody, long durationMs) {
        this.status = "SUCCESS";
        this.httpStatusCode = statusCode;
        this.responseBody = responseBody != null && responseBody.length() > 5000
                ? responseBody.substring(0, 5000)
                : responseBody;
        this.durationMs = durationMs;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Mark delivery as failed
     */
    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage != null && errorMessage.length() > 2000
                ? errorMessage.substring(0, 2000)
                : errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark delivery for retry
     */
    public void markForRetry(LocalDateTime nextRetryAt) {
        this.status = "RETRYING";
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
    }
}
