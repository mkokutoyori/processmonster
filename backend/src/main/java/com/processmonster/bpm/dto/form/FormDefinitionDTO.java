package com.processmonster.bpm.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for FormDefinition entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinitionDTO {

    private Long id;
    private String formKey;
    private String name;
    private String description;
    private String category;
    private Integer version;
    private String schemaJson;
    private String uiSchemaJson;
    private Boolean published;
    private Boolean isLatestVersion;
    private String processDefinitionKey;
    private String taskDefinitionKey;

    // Audit fields
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // Counts
    private Long submissionCount;
}
