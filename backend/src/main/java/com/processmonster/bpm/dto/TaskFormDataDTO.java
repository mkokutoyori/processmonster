package com.processmonster.bpm.dto;

import com.processmonster.bpm.entity.FormDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for task form data
 * Combines task information with form definition and initial values
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFormDataDTO {

    /**
     * Task ID
     */
    private Long taskId;

    /**
     * Task name
     */
    private String taskName;

    /**
     * Form key from BPMN
     */
    private String formKey;

    /**
     * Form definition with schema
     */
    private FormDefinition formDefinition;

    /**
     * Initial values for form fields (pre-filled from process variables)
     */
    private Map<String, Object> initialValues;

    /**
     * All process variables
     */
    private Map<String, Object> processVariables;

    /**
     * Whether this form is read-only (for completed tasks)
     */
    private boolean readOnly;
}
