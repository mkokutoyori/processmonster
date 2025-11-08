package com.processmonster.bpm.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for webhook response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Webhook configuration")
public class WebhookDTO {

    @Schema(description = "Webhook ID", example = "1")
    private Long id;

    @Schema(description = "Webhook name", example = "Slack Notifications")
    private String name;

    @Schema(description = "Target URL", example = "https://hooks.slack.com/services/...")
    private String url;

    @Schema(description = "Description", example = "Send process events to Slack")
    private String description;

    @Schema(description = "Whether the webhook is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Event types that trigger this webhook")
    private Set<String> events;

    @Schema(description = "HTTP method", example = "POST")
    private String httpMethod;

    @Schema(description = "Content type", example = "application/json")
    private String contentType;

    @Schema(description = "Timeout in milliseconds", example = "30000")
    private Integer timeoutMs;

    @Schema(description = "Max retry attempts", example = "3")
    private Integer maxRetries;

    @Schema(description = "Retry delay in milliseconds", example = "1000")
    private Integer retryDelayMs;

    @Schema(description = "Total successful deliveries", example = "1250")
    private Long successCount;

    @Schema(description = "Total failed deliveries", example = "15")
    private Long failureCount;

    @Schema(description = "Last successful delivery timestamp")
    private LocalDateTime lastSuccessAt;

    @Schema(description = "Last failed delivery timestamp")
    private LocalDateTime lastFailureAt;

    @Schema(description = "Last error message")
    private String lastError;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Created by user")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated by user")
    private String updatedBy;

    @Schema(description = "Whether the webhook is active", example = "true")
    private Boolean active;
}
