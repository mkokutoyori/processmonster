package com.processmonster.bpm.dto.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ProcessInstance (response)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstanceDTO {

    private Long id;
    private Long processDefinitionId;
    private String processDefinitionName;
    private String processKey;
    private Integer processVersion;
    private String businessKey;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;
    private String startedBy;
    private String currentActivityId;
    private String currentActivityName;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
