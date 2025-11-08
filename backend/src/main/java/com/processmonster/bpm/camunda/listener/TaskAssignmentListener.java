package com.processmonster.bpm.camunda.listener;

import com.processmonster.bpm.service.NotificationService;
import com.processmonster.bpm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

/**
 * Camunda Task Assignment Listener
 * Handles task assignments and sends notifications
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAssignmentListener implements TaskListener {

    private final TaskService taskService;
    private final NotificationService notificationService;

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("Task assigned in Camunda - ID: {}, Assignee: {}",
                delegateTask.getId(), delegateTask.getAssignee());

        try {
            // Get the ProcessMonster task ID from Camunda variables
            Object taskIdObj = delegateTask.getVariable("processMonsterTaskId");

            if (taskIdObj != null && delegateTask.getAssignee() != null) {
                Long processMonsterTaskId = Long.valueOf(taskIdObj.toString());
                Long assigneeId = extractUserId(delegateTask.getAssignee());

                if (assigneeId != null) {
                    // Update task assignment in ProcessMonster
                    taskService.assignTaskFromCamunda(processMonsterTaskId, assigneeId);

                    log.info("Task assigned in ProcessMonster - Task ID: {}, Assignee ID: {}",
                            processMonsterTaskId, assigneeId);

                    // Send notification to assignee
                    sendAssignmentNotification(assigneeId, delegateTask);
                }
            }

        } catch (Exception e) {
            log.error("Error handling task assignment", e);
            // Don't throw exception to avoid disrupting Camunda workflow
        }
    }

    /**
     * Send notification to the assigned user
     */
    private void sendAssignmentNotification(Long userId, DelegateTask task) {
        try {
            String message = String.format(
                "You have been assigned a new task: %s",
                task.getName()
            );

            notificationService.sendTaskAssignmentNotification(
                userId,
                task.getName(),
                task.getDescription(),
                task.getDueDate()
            );

            log.debug("Assignment notification sent to user: {}", userId);

        } catch (Exception e) {
            log.error("Error sending assignment notification", e);
        }
    }

    /**
     * Extract user ID from assignee string
     */
    private Long extractUserId(String assignee) {
        try {
            return Long.parseLong(assignee);
        } catch (NumberFormatException e) {
            log.warn("Could not parse assignee as user ID: {}", assignee);
            return null;
        }
    }
}
