package com.processmonster.bpm.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for process definition statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistics for a specific process definition")
public class ProcessDefinitionStatsDTO {

    @Schema(description = "Process definition key", example = "loan-approval")
    private String processDefinitionKey;

    @Schema(description = "Total instances of this process", example = "120")
    private Long total;

    @Schema(description = "Currently active instances", example = "15")
    private Long active;

    @Schema(description = "Completed instances", example = "95")
    private Long completed;

    @Schema(description = "Failed instances", example = "10")
    private Long failed;

    @Schema(description = "Average duration in hours for completed processes", example = "8.5")
    private Double avgDurationHours;
}
