package com.processmonster.bpm.camunda.listener;

import com.processmonster.bpm.dto.TaskCreateDTO;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.TaskStatus;
import com.processmonster.bpm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Camunda Task Create Listener
 * Automatically creates tasks in ProcessMonster system when Camunda creates tasks
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreateListener implements TaskListener {

    private final TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("Task created in Camunda - ID: {}, Name: {}",
                delegateTask.getId(), delegateTask.getName());

        try {
            // Create task in our system
            TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
            taskCreateDTO.setTitle(delegateTask.getName());
            taskCreateDTO.setDescription(delegateTask.getDescription());
            taskCreateDTO.setPriority(mapPriority(delegateTask.getPriority()));
            taskCreateDTO.setStatus(TaskStatus.PENDING);

            // Extract assignee if present
            if (delegateTask.getAssignee() != null) {
                taskCreateDTO.setAssigneeId(extractUserId(delegateTask.getAssignee()));
            }

            // Extract process instance ID
            taskCreateDTO.setProcessInstanceId(delegateTask.getProcessInstanceId());

            // Store Camunda task ID for synchronization
            taskCreateDTO.setCamundaTaskId(delegateTask.getId());

            // Extract due date if present
            if (delegateTask.getDueDate() != null) {
                taskCreateDTO.setDueDate(
                    LocalDateTime.ofInstant(
                        delegateTask.getDueDate().toInstant(),
                        java.time.ZoneId.systemDefault()
                    )
                );
            }

            // Extract form key if present (for form-task integration)
            String formKey = delegateTask.getFormKey();
            if (formKey != null) {
                taskCreateDTO.setFormKey(formKey);
            }

            // Create the task
            Task createdTask = taskService.createTaskFromCamunda(taskCreateDTO);

            log.info("Task created in ProcessMonster - ID: {}, Camunda Task ID: {}",
                    createdTask.getId(), delegateTask.getId());

            // Store the ProcessMonster task ID in Camunda task variables
            delegateTask.setVariable("processMonsterTaskId", createdTask.getId());

        } catch (Exception e) {
            log.error("Error creating task in ProcessMonster system", e);
            // Don't throw exception to avoid disrupting Camunda workflow
        }
    }

    /**
     * Map Camunda priority to ProcessMonster priority
     */
    private String mapPriority(int camundaPriority) {
        if (camundaPriority >= 75) {
            return "URGENT";
        } else if (camundaPriority >= 50) {
            return "HIGH";
        } else if (camundaPriority >= 25) {
            return "MEDIUM";
        } else {
            return "LOW";
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
