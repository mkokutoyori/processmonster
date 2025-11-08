package com.processmonster.bpm.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for webhook delivery information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Webhook delivery attempt information")
public class WebhookDeliveryDTO {

    @Schema(description = "Delivery ID", example = "1")
    private Long id;

    @Schema(description = "Webhook ID", example = "1")
    private Long webhookId;

    @Schema(description = "Webhook name", example = "Slack Notifications")
    private String webhookName;

    @Schema(description = "Event type that triggered this delivery", example = "process.completed")
    private String eventType;

    @Schema(description = "Request payload (JSON)")
    private String requestPayload;

    @Schema(description = "HTTP status code", example = "200")
    private Integer httpStatusCode;

    @Schema(description = "Response body")
    private String responseBody;

    @Schema(description = "Delivery status", example = "SUCCESS")
    private String status;

    @Schema(description = "Number of retry attempts", example = "0")
    private Integer retryCount;

    @Schema(description = "Error message if delivery failed")
    private String errorMessage;

    @Schema(description = "Request duration in milliseconds", example = "125")
    private Long durationMs;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Completion timestamp")
    private LocalDateTime completedAt;

    @Schema(description = "Next retry timestamp (for RETRYING status)")
    private LocalDateTime nextRetryAt;
}
