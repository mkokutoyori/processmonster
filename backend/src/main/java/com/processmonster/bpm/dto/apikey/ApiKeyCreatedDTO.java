package com.processmonster.bpm.dto.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned when an API key is created.
 * This is the ONLY time the plain API key is shown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response when API key is created (includes plain key - shown only once!)")
public class ApiKeyCreatedDTO {

    @Schema(description = "The API key information")
    private ApiKeyDTO apiKey;

    @Schema(description = "The plain API key (SAVE THIS! It will never be shown again)",
            example = "pk_live_abc123xyz789...")
    private String plainKey;

    @Schema(description = "Warning message", example = "Save this key securely! It will never be shown again.")
    private String warning;
}
