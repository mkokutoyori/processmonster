package com.processmonster.bpm.dto.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new ProcessDefinition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcessDefinitionDTO {

    @NotBlank(message = "{validation.required}")
    @Size(min = 2, max = 200, message = "{validation.size}")
    private String name;

    @NotBlank(message = "{validation.required}")
    private String bpmnXml;

    @Size(max = 1000, message = "{validation.size.max}")
    private String description;

    private Long categoryId;

    @Builder.Default
    private Boolean isTemplate = false;

    @Builder.Default
    private Boolean published = false;

    @Builder.Default
    private Boolean active = true;

    @Size(max = 500, message = "{validation.size.max}")
    private String tags;
}
