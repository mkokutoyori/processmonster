package com.processmonster.bpm.dto.process;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing ProcessDefinition
 * Updating the bpmnXml creates a new version
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcessDefinitionDTO {

    @Size(min = 2, max = 200, message = "{validation.size}")
    private String name;

    /**
     * If bpmnXml is provided, a new version will be created automatically
     */
    private String bpmnXml;

    @Size(max = 1000, message = "{validation.size.max}")
    private String description;

    private Long categoryId;

    private Boolean isTemplate;

    private Boolean published;

    private Boolean active;

    @Size(max = 500, message = "{validation.size.max}")
    private String tags;
}
