package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.TaskStatus;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
import com.processmonster.bpm.repository.TaskRepository;
import com.processmonster.bpm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating metrics and KPIs for dashboard and reporting.
 * Results are cached for 5 minutes to improve performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MetricsService {

    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Get overall system KPIs.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'system-kpis'")
    public Map<String, Object> getSystemKPIs() {
        log.debug("Calculating system KPIs");

        Map<String, Object> kpis = new HashMap<>();

        // Process metrics
        kpis.put("activeProcesses", countActiveProcesses());
        kpis.put("completedProcessesToday", countCompletedProcessesToday());
        kpis.put("failedProcessesToday", countFailedProcessesToday());
        kpis.put("totalProcesses", processInstanceRepository.countByDeletedFalse());

        // Task metrics
        kpis.put("activeTasks", countActiveTasks());
        kpis.put("overdueTasks", countOverdueTasks());
        kpis.put("tasksCompletedToday", countTasksCompletedToday());
        kpis.put("totalTasks", taskRepository.countByDeletedFalse());

        // Performance metrics
        kpis.put("avgTaskCompletionTimeMinutes", getAverageTaskCompletionTime());
        kpis.put("avgProcessDurationHours", getAverageProcessDuration());

        // User metrics
        kpis.put("activeUsers", userRepository.countByEnabledTrueAndDeletedFalse());
        kpis.put("totalUsers", userRepository.countByDeletedFalse());

        log.debug("System KPIs calculated: {}", kpis);
        return kpis;
    }

    /**
     * Get process statistics grouped by status.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'process-stats'")
    public Map<String, Long> getProcessStatsByStatus() {
        log.debug("Calculating process statistics by status");

        List<ProcessInstance> processes = processInstanceRepository.findByDeletedFalse();

        return processes.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Get task statistics grouped by status.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'task-stats'")
    public Map<String, Long> getTaskStatsByStatus() {
        log.debug("Calculating task statistics by status");

        List<Task> tasks = taskRepository.findByDeletedFalse();

        return tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Get task statistics grouped by priority.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'task-priority-stats'")
    public Map<String, Long> getTaskStatsByPriority() {
        log.debug("Calculating task statistics by priority");

        List<Task> tasks = taskRepository.findByDeletedFalse();

        return tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Get user task performance statistics.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'user-task-stats-' + #username")
    public Map<String, Object> getUserTaskStats(String username) {
        log.debug("Calculating task statistics for user: {}", username);

        Map<String, Object> stats = new HashMap<>();

        stats.put("assigned", taskRepository.countByAssigneeUsernameAndDeletedFalse(username));
        stats.put("completed", taskRepository.countByAssigneeUsernameAndStatusAndDeletedFalse(
                username, TaskStatus.COMPLETED));
        stats.put("inProgress", taskRepository.countByAssigneeUsernameAndStatusAndDeletedFalse(
                username, TaskStatus.IN_PROGRESS));
        stats.put("overdue", countOverdueTasksByUser(username));
        stats.put("avgCompletionTimeMinutes", getAverageTaskCompletionTimeByUser(username));

        return stats;
    }

    /**
     * Get daily task completion trend for last N days.
     * Cached for 5 minutes.
     */
    @Cacheable(value = "metrics", key = "'daily-completion-trend-' + #days")
    public Map<String, Long> getDailyTaskCompletionTrend(int days) {
        log.debug("Calculating daily task completion trend for {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Task> completedTasks = taskRepository.findByStatusAndCompletedAtAfterAndDeletedFalse(
                TaskStatus.COMPLETED, startDate);

        return completedTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCompletedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
    }

    /**
     * Get process statistics for a specific process definition.
     */
    @Cacheable(value = "metrics", key = "'process-def-stats-' + #processDefinitionKey")
    public Map<String, Object> getProcessDefinitionStats(String processDefinitionKey) {
        log.debug("Calculating statistics for process definition: {}", processDefinitionKey);

        List<ProcessInstance> instances = processInstanceRepository
                .findByProcessDefinitionKeyAndDeletedFalse(processDefinitionKey);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", instances.size());
        stats.put("active", instances.stream()
                .filter(p -> p.getStatus() == ProcessInstance.ProcessInstanceStatus.RUNNING)
                .count());
        stats.put("completed", instances.stream()
                .filter(p -> p.getStatus() == ProcessInstance.ProcessInstanceStatus.COMPLETED)
                .count());
        stats.put("failed", instances.stream()
                .filter(p -> p.getStatus() == ProcessInstance.ProcessInstanceStatus.FAILED)
                .count());

        // Calculate average duration for completed processes
        double avgDuration = instances.stream()
                .filter(p -> p.getStatus() == ProcessInstance.ProcessInstanceStatus.COMPLETED
                        && p.getEndTime() != null)
                .mapToDouble(p -> Duration.between(p.getStartTime(), p.getEndTime()).toHours())
                .average()
                .orElse(0.0);
        stats.put("avgDurationHours", avgDuration);

        return stats;
    }

    // Private helper methods

    private long countActiveProcesses() {
        return processInstanceRepository.countByStatusAndDeletedFalse(
                ProcessInstance.ProcessInstanceStatus.RUNNING);
    }

    private long countCompletedProcessesToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return processInstanceRepository.countByStatusAndEndTimeAfterAndDeletedFalse(
                ProcessInstance.ProcessInstanceStatus.COMPLETED, startOfDay);
    }

    private long countFailedProcessesToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return processInstanceRepository.countByStatusAndEndTimeAfterAndDeletedFalse(
                ProcessInstance.ProcessInstanceStatus.FAILED, startOfDay);
    }

    private long countActiveTasks() {
        return taskRepository.countByStatusInAndDeletedFalse(
                List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS));
    }

    private long countOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now()).size();
    }

    private long countTasksCompletedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return taskRepository.countByStatusAndCompletedAtAfterAndDeletedFalse(
                TaskStatus.COMPLETED, startOfDay);
    }

    private double getAverageTaskCompletionTime() {
        List<Task> completedTasks = taskRepository.findByStatusAndDeletedFalse(
                TaskStatus.COMPLETED);

        return completedTasks.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCreatedAt() != null)
                .mapToDouble(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                .average()
                .orElse(0.0);
    }

    private double getAverageProcessDuration() {
        List<ProcessInstance> completedProcesses = processInstanceRepository.findByStatusAndDeletedFalse(
                ProcessInstance.ProcessInstanceStatus.COMPLETED);

        return completedProcesses.stream()
                .filter(p -> p.getEndTime() != null)
                .mapToDouble(p -> Duration.between(p.getStartTime(), p.getEndTime()).toHours())
                .average()
                .orElse(0.0);
    }

    private long countOverdueTasksByUser(String username) {
        return taskRepository.findOverdueTasks(LocalDateTime.now()).stream()
                .filter(t -> username.equals(t.getAssignee() != null ? t.getAssignee().getUsername() : null))
                .count();
    }

    private double getAverageTaskCompletionTimeByUser(String username) {
        List<Task> completedTasks = taskRepository.findByAssigneeUsernameAndStatusAndDeletedFalse(
                username, TaskStatus.COMPLETED);

        return completedTasks.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCreatedAt() != null)
                .mapToDouble(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                .average()
                .orElse(0.0);
    }
}
