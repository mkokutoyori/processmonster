package com.processmonster.bpm.entity;

/**
 * Submission status enum
 */
public enum SubmissionStatus {
    /**
     * Form is being filled (auto-saved)
     */
    DRAFT,

    /**
     * Form has been submitted
     */
    SUBMITTED,

    /**
     * Form submission has been approved
     */
    APPROVED,

    /**
     * Form submission has been rejected
     */
    REJECTED,

    /**
     * Form submission was cancelled
     */
    CANCELLED
}
