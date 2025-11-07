package com.processmonster.bpm.exception;

/**
 * Business Exception
 *
 * Custom exception for business logic errors.
 * Used to throw domain-specific errors that should be handled gracefully
 * and returned as 400 Bad Request to the client.
 *
 * Examples:
 * - User already exists
 * - Invalid state transition
 * - Business rule violation
 * - Insufficient permissions for operation
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
public class BusinessException extends RuntimeException {

    /**
     * Constructs a new BusinessException with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constructs a new BusinessException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
