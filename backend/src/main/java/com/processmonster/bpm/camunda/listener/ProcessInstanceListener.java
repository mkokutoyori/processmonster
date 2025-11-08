package com.processmonster.bpm.camunda.listener;

import com.processmonster.bpm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Camunda Process Instance Listener
 * Tracks process lifecycle events (start, end, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessInstanceListener implements ExecutionListener {

    private final NotificationService notificationService;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String eventName = execution.getEventName();
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionKey = execution.getProcessDefinitionId();

        log.info("Process instance event - Event: {}, Instance ID: {}, Definition: {}",
                eventName, processInstanceId, processDefinitionKey);

        switch (eventName) {
            case "start":
                handleProcessStart(execution);
                break;
            case "end":
                handleProcessEnd(execution);
                break;
            default:
                log.debug("Unhandled process event: {}", eventName);
        }
    }

    /**
     * Handle process instance start event
     */
    private void handleProcessStart(DelegateExecution execution) {
        log.info("Process started - ID: {}, Business Key: {}",
                execution.getProcessInstanceId(),
                execution.getProcessBusinessKey());

        try {
            // Extract initiator from variables
            Object initiatorObj = execution.getVariable("initiatorId");
            if (initiatorObj != null) {
                Long initiatorId = Long.valueOf(initiatorObj.toString());

                // Send notification
                notificationService.sendProcessStartNotification(
                    initiatorId,
                    execution.getProcessDefinitionId(),
                    execution.getProcessInstanceId()
                );
            }

        } catch (Exception e) {
            log.error("Error handling process start", e);
        }
    }

    /**
     * Handle process instance end event
     */
    private void handleProcessEnd(DelegateExecution execution) {
        log.info("Process completed - ID: {}, Business Key: {}",
                execution.getProcessInstanceId(),
                execution.getProcessBusinessKey());

        try {
            // Extract initiator from variables
            Object initiatorObj = execution.getVariable("initiatorId");
            if (initiatorObj != null) {
                Long initiatorId = Long.valueOf(initiatorObj.toString());

                // Send completion notification
                notificationService.sendProcessCompletionNotification(
                    initiatorId,
                    execution.getProcessDefinitionId(),
                    execution.getProcessInstanceId()
                );
            }

        } catch (Exception e) {
            log.error("Error handling process end", e);
        }
    }
}
