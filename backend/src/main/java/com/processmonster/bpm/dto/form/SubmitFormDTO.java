package com.processmonster.bpm.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting a form
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitFormDTO {

    @NotNull(message = "{form.definition-id.required}")
    private Long formDefinitionId;

    @NotBlank(message = "{form.data.required}")
    private String dataJson;

    @Size(max = 200, message = "{form.business-key.size}")
    private String businessKey;

    private Long taskId;
    private Long processInstanceId;

    @Size(max = 1000, message = "{form.notes.size}")
    private String notes;
}
