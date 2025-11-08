package com.processmonster.bpm.dto.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ExecutionHistory (response)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionHistoryDTO {

    private Long id;
    private String eventType;
    private String activityId;
    private String activityName;
    private String activityType;
    private LocalDateTime timestamp;
    private Long durationMillis;
    private String performedBy;
    private String eventDetails;
    private String errorMessage;
}
