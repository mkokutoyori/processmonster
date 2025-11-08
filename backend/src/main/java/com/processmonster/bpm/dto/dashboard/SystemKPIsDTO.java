package com.processmonster.bpm.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for system-wide Key Performance Indicators
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System KPIs including process and task metrics")
public class SystemKPIsDTO {

    @Schema(description = "Number of currently active processes", example = "42")
    private Long activeProcesses;

    @Schema(description = "Number of processes completed today", example = "15")
    private Long completedProcessesToday;

    @Schema(description = "Number of processes that failed today", example = "2")
    private Long failedProcessesToday;

    @Schema(description = "Total number of processes (all time)", example = "1250")
    private Long totalProcesses;

    @Schema(description = "Number of currently active tasks", example = "87")
    private Long activeTasks;

    @Schema(description = "Number of overdue tasks", example = "5")
    private Long overdueTasks;

    @Schema(description = "Number of tasks completed today", example = "23")
    private Long tasksCompletedToday;

    @Schema(description = "Total number of tasks (all time)", example = "3450")
    private Long totalTasks;

    @Schema(description = "Average task completion time in minutes", example = "42.5")
    private Double avgTaskCompletionTimeMinutes;

    @Schema(description = "Average process duration in hours", example = "12.3")
    private Double avgProcessDurationHours;

    @Schema(description = "Number of active users", example = "25")
    private Long activeUsers;

    @Schema(description = "Total number of users", example = "50")
    private Long totalUsers;
}
