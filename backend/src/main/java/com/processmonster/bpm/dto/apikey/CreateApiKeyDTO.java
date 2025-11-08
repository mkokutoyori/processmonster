package com.processmonster.bpm.dto.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for creating a new API key
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new API key")
public class CreateApiKeyDTO {

    @NotBlank(message = "Name is required")
    @Schema(description = "API key name", example = "Production API Key", required = true)
    private String name;

    @Schema(description = "Description of the API key", example = "API key for production integration")
    private String description;

    @NotNull(message = "Permissions are required")
    @Schema(description = "Permissions to grant to this API key", required = true)
    private Set<String> permissions;

    @Min(value = 1, message = "Rate limit must be at least 1")
    @Schema(description = "Rate limit: max requests per minute", example = "100")
    private Integer rateLimitPerMinute;

    @Schema(description = "Expiration date (null if never expires)")
    private LocalDateTime expiresAt;

    @Schema(description = "Allowed IP addresses (comma-separated, null for any IP)")
    private String allowedIps;
}
