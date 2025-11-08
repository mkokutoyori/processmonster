package com.processmonster.bpm.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for creating a new webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new webhook")
public class CreateWebhookDTO {

    @NotBlank(message = "Name is required")
    @Schema(description = "Webhook name", example = "Slack Notifications", required = true)
    private String name;

    @NotBlank(message = "URL is required")
    @Schema(description = "Target URL", example = "https://hooks.slack.com/services/...", required = true)
    private String url;

    @Schema(description = "Description", example = "Send process events to Slack")
    private String description;

    @NotNull(message = "Events are required")
    @Schema(description = "Event types that trigger this webhook", required = true)
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
