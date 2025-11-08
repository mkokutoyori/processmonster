package com.processmonster.bpm.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new form definition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFormDefinitionDTO {

    @NotBlank(message = "{form.key.required}")
    @Size(max = 100, message = "{form.key.size}")
    private String formKey;

    @NotBlank(message = "{form.name.required}")
    @Size(max = 200, message = "{form.name.size}")
    private String name;

    @Size(max = 1000, message = "{form.description.size}")
    private String description;

    @Size(max = 50, message = "{form.category.size}")
    private String category;

    @NotBlank(message = "{form.schema.required}")
    private String schemaJson;

    private String uiSchemaJson;

    @Size(max = 100, message = "{form.process-key.size}")
    private String processDefinitionKey;

    @Size(max = 100, message = "{form.task-key.size}")
    private String taskDefinitionKey;
}
