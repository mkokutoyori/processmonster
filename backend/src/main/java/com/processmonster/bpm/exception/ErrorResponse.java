package com.processmonster.bpm.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard Error Response DTO
 *
 * Consistent error response structure for all API errors.
 * Includes timestamp, status, error message, and optional field errors for validation.
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * HTTP status reason phrase
     */
    private String error;

    /**
     * Error message (internationalized)
     */
    private String message;

    /**
     * Request path where error occurred
     */
    private String path;

    /**
     * Field-level validation errors (for 400 Bad Request)
     * Key: field name, Value: error message
     */
    private Map<String, String> fieldErrors;

    /**
     * Additional error details (optional)
     */
    private Map<String, Object> details;
}
