package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.dashboard.*;
import com.processmonster.bpm.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for dashboard and metrics endpoints
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard and metrics API")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final MetricsService metricsService;

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get system KPIs", description = "Retrieve overall system key performance indicators")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KPIs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<SystemKPIsDTO> getSystemKPIs() {
        log.debug("REST request to get system KPIs");

        Map<String, Object> kpis = metricsService.getSystemKPIs();

        SystemKPIsDTO dto = SystemKPIsDTO.builder()
                .activeProcesses((Long) kpis.get("activeProcesses"))
                .completedProcessesToday((Long) kpis.get("completedProcessesToday"))
                .failedProcessesToday((Long) kpis.get("failedProcessesToday"))
                .totalProcesses((Long) kpis.get("totalProcesses"))
                .activeTasks((Long) kpis.get("activeTasks"))
                .overdueTasks((Long) kpis.get("overdueTasks"))
                .tasksCompletedToday((Long) kpis.get("tasksCompletedToday"))
                .totalTasks((Long) kpis.get("totalTasks"))
                .avgTaskCompletionTimeMinutes((Double) kpis.get("avgTaskCompletionTimeMinutes"))
                .avgProcessDurationHours((Double) kpis.get("avgProcessDurationHours"))
                .activeUsers((Long) kpis.get("activeUsers"))
                .totalUsers((Long) kpis.get("totalUsers"))
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/process-stats")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get process statistics by status", description = "Retrieve process instance counts grouped by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<StatusStatsDTO> getProcessStatsByStatus() {
        log.debug("REST request to get process statistics by status");

        Map<String, Long> stats = metricsService.getProcessStatsByStatus();

        return ResponseEntity.ok(StatusStatsDTO.builder()
                .stats(stats)
                .build());
    }

    @GetMapping("/task-stats")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get task statistics by status", description = "Retrieve task counts grouped by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<StatusStatsDTO> getTaskStatsByStatus() {
        log.debug("REST request to get task statistics by status");

        Map<String, Long> stats = metricsService.getTaskStatsByStatus();

        return ResponseEntity.ok(StatusStatsDTO.builder()
                .stats(stats)
                .build());
    }

    @GetMapping("/task-priority-stats")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get task statistics by priority", description = "Retrieve task counts grouped by priority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<StatusStatsDTO> getTaskStatsByPriority() {
        log.debug("REST request to get task statistics by priority");

        Map<String, Long> stats = metricsService.getTaskStatsByPriority();

        return ResponseEntity.ok(StatusStatsDTO.builder()
                .stats(stats)
                .build());
    }

    @GetMapping("/user-stats")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get user task statistics", description = "Retrieve task performance statistics for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserTaskStatsDTO> getUserTaskStats(
            @Parameter(description = "Username to get stats for (defaults to current user)")
            @RequestParam(required = false) String username,
            @AuthenticationPrincipal UserDetails currentUser) {

        String targetUser = username != null ? username : currentUser.getUsername();
        log.debug("REST request to get task statistics for user: {}", targetUser);

        Map<String, Object> stats = metricsService.getUserTaskStats(targetUser);

        UserTaskStatsDTO dto = UserTaskStatsDTO.builder()
                .assigned((Long) stats.get("assigned"))
                .completed((Long) stats.get("completed"))
                .inProgress((Long) stats.get("inProgress"))
                .overdue((Long) stats.get("overdue"))
                .avgCompletionTimeMinutes((Double) stats.get("avgCompletionTimeMinutes"))
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/completion-trend")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get daily completion trend", description = "Retrieve task completion trend for last N days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trend retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DailyCompletionTrendDTO> getDailyCompletionTrend(
            @Parameter(description = "Number of days to include (default: 7, max: 90)")
            @RequestParam(defaultValue = "7") int days) {

        if (days < 1 || days > 90) {
            log.warn("Invalid days parameter: {}", days);
            return ResponseEntity.badRequest().build();
        }

        log.debug("REST request to get daily completion trend for {} days", days);

        Map<String, Long> dailyCompletions = metricsService.getDailyTaskCompletionTrend(days);

        DailyCompletionTrendDTO dto = DailyCompletionTrendDTO.builder()
                .dailyCompletions(dailyCompletions)
                .days(days)
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/process-definition-stats/{processDefinitionKey}")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_VIEW', 'PROCESS_READ', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ANALYST')")
    @Operation(summary = "Get process definition statistics", description = "Retrieve statistics for a specific process definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Process definition not found")
    })
    public ResponseEntity<ProcessDefinitionStatsDTO> getProcessDefinitionStats(
            @Parameter(description = "Process definition key", example = "loan-approval")
            @PathVariable String processDefinitionKey) {

        log.debug("REST request to get statistics for process definition: {}", processDefinitionKey);

        Map<String, Object> stats = metricsService.getProcessDefinitionStats(processDefinitionKey);

        ProcessDefinitionStatsDTO dto = ProcessDefinitionStatsDTO.builder()
                .processDefinitionKey(processDefinitionKey)
                .total((Long) stats.get("total"))
                .active((Long) stats.get("active"))
                .completed((Long) stats.get("completed"))
                .failed((Long) stats.get("failed"))
                .avgDurationHours((Double) stats.get("avgDurationHours"))
                .build();

        return ResponseEntity.ok(dto);
    }
}
