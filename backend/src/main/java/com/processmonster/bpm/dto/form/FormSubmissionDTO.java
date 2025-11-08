package com.processmonster.bpm.dto.form;

import com.processmonster.bpm.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for FormSubmission entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmissionDTO {

    private Long id;

    // Form info
    private Long formDefinitionId;
    private String formKey;
    private String formName;
    private Integer formVersion;

    // Associated entities
    private Long taskId;
    private Long processInstanceId;

    // Submission data
    private String dataJson;
    private SubmissionStatus status;
    private String submittedBy;
    private LocalDateTime submittedAt;

    // Business info
    private String businessKey;
    private String notes;

    // Validation
    private String validationErrors;

    // Audit fields
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
