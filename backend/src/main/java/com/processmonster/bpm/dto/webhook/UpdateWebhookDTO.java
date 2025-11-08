package com.processmonster.bpm.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for updating a webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a webhook")
public class UpdateWebhookDTO {

    @Schema(description = "Webhook name", example = "Slack Notifications")
    private String name;

    @Schema(description = "Target URL", example = "https://hooks.slack.com/services/...")
    private String url;

    @Schema(description = "Description", example = "Send process events to Slack")
    private String description;

    @Schema(description = "Event types that trigger this webhook")
    private Set<String> events;

    @Schema(description = "Secret for HMAC signature verification")
    private String secret;

    @Schema(description = "Custom headers (JSON format)")
    private String customHeaders;

    @Schema(description = "HTTP method", example = "POST")
    private String httpMethod;

    @Schema(description = "Content type", example = "application/json")
    private String contentType;

    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Schema(description = "Timeout in milliseconds", example = "30000")
    private Integer timeoutMs;

    @Min(value = 0, message = "Max retries must be at least 0")
    @Schema(description = "Max retry attempts", example = "3")
    private Integer maxRetries;

    @Min(value = 100, message = "Retry delay must be at least 100ms")
    @Schema(description = "Retry delay in milliseconds", example = "1000")
    private Integer retryDelayMs;
}
