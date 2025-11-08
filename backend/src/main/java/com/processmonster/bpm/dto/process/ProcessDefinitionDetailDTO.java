package com.processmonster.bpm.dto.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for ProcessDefinition entity with BPMN XML (response for detail endpoint)
 * Extends ProcessDefinitionDTO and adds the bpmnXml field
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionDetailDTO extends ProcessDefinitionDTO {

    /**
     * BPMN 2.0 XML content (only included in detail view)
     */
    private String bpmnXml;
}
