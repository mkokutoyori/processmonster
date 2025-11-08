package com.processmonster.bpm.camunda.listener;

import com.processmonster.bpm.entity.TaskStatus;
import com.processmonster.bpm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

/**
 * Camunda Task Complete Listener
 * Automatically updates tasks in ProcessMonster system when Camunda tasks are completed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCompleteListener implements TaskListener {

    private final TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("Task completed in Camunda - ID: {}, Name: {}",
                delegateTask.getId(), delegateTask.getName());

        try {
            // Get the ProcessMonster task ID from Camunda variables
            Object taskIdObj = delegateTask.getVariable("processMonsterTaskId");

            if (taskIdObj != null) {
                Long processMonsterTaskId = Long.valueOf(taskIdObj.toString());

                // Update task status to COMPLETED
                taskService.updateTaskStatusFromCamunda(processMonsterTaskId, TaskStatus.COMPLETED);

                log.info("Task updated in ProcessMonster - ID: {}, Status: COMPLETED",
                        processMonsterTaskId);

                // Extract completion data if available
                if (delegateTask.getVariables().containsKey("taskCompletionData")) {
                    Object completionData = delegateTask.getVariable("taskCompletionData");
                    log.debug("Task completion data: {}", completionData);
                    // Store completion data if needed
                }

            } else {
                log.warn("No ProcessMonster task ID found for Camunda task: {}",
                        delegateTask.getId());
            }

        } catch (Exception e) {
            log.error("Error updating task in ProcessMonster system", e);
            // Don't throw exception to avoid disrupting Camunda workflow
        }
    }
}
