package com.processmonster.bpm.dto.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for API key response (never includes the actual key after creation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API key information")
public class ApiKeyDTO {

    @Schema(description = "API key ID", example = "1")
    private Long id;

    @Schema(description = "API key name", example = "Production API Key")
    private String name;

    @Schema(description = "API key prefix for display", example = "pk_live_12345678...")
    private String keyPrefix;

    @Schema(description = "Description of the API key", example = "API key for production integration")
    private String description;

    @Schema(description = "Whether the API key is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Expiration date (null if never expires)")
    private LocalDateTime expiresAt;

    @Schema(description = "Last time this API key was used")
    private LocalDateTime lastUsedAt;

    @Schema(description = "Allowed IP addresses (comma-separated)")
    private String allowedIps;

    @Schema(description = "Permissions granted to this API key")
    private Set<String> permissions;

    @Schema(description = "Rate limit: max requests per minute", example = "100")
    private Integer rateLimitPerMinute;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Created by user")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated by user")
    private String updatedBy;

    @Schema(description = "Whether the API key is active (enabled and not expired)", example = "true")
    private Boolean active;

    @Schema(description = "Whether the API key is expired", example = "false")
    private Boolean expired;
}
