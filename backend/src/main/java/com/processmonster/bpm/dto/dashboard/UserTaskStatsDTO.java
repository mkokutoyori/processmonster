package com.processmonster.bpm.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user task performance statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User task performance statistics")
public class UserTaskStatsDTO {

    @Schema(description = "Total tasks assigned to user", example = "25")
    private Long assigned;

    @Schema(description = "Tasks completed by user", example = "18")
    private Long completed;

    @Schema(description = "Tasks currently in progress", example = "5")
    private Long inProgress;

    @Schema(description = "Overdue tasks for user", example = "2")
    private Long overdue;

    @Schema(description = "Average task completion time in minutes", example = "38.5")
    private Double avgCompletionTimeMinutes;
}
