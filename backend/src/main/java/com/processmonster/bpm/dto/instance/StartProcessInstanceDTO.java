package com.processmonster.bpm.dto.instance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for starting a new process instance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartProcessInstanceDTO {

    @NotNull(message = "{validation.required}")
    private Long processDefinitionId;

    private String businessKey;

    private Map<String, Object> variables;
}
