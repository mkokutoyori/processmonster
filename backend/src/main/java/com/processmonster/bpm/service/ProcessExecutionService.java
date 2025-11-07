package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ExecutionHistory;
import com.processmonster.bpm.entity.ExecutionHistory.EventType;
import com.processmonster.bpm.entity.ProcessDefinition;
import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.ProcessInstance.ProcessInstanceStatus;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.ExecutionHistoryRepository;
import com.processmonster.bpm.repository.ProcessDefinitionRepository;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for executing and managing process instances
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessExecutionService {

    private final ProcessInstanceRepository instanceRepository;
    private final ProcessDefinitionRepository definitionRepository;
    private final ExecutionHistoryRepository historyRepository;
    private final VariableService variableService;
    private final MessageSource messageSource;

    /**
     * Start a new process instance
     */
    @Transactional
    public ProcessInstance startProcess(Long processDefinitionId, String businessKey, Map<String, Object> variables) {
        log.debug("Starting process instance for definition: {}", processDefinitionId);

        ProcessDefinition definition = findDefinitionById(processDefinitionId);

        if (!definition.getPublished()) {
            throw new BusinessException(getMessage("instance.start.not-published"));
        }

        if (!definition.getActive()) {
            throw new BusinessException(getMessage("instance.start.not-active"));
        }

        // Create process instance
        ProcessInstance instance = ProcessInstance.builder()
                .processDefinition(definition)
                .businessKey(businessKey)
                .status(ProcessInstanceStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .startedBy(getCurrentUsername())
                .build();

        ProcessInstance saved = instanceRepository.save(instance);

        // Set initial variables
        if (variables != null && !variables.isEmpty()) {
            variableService.setVariables(saved.getId(), variables);
        }

        // Record history event
        recordHistory(saved, EventType.PROCESS_STARTED, "Process started");

        log.info("Started process instance {} for definition {} (key: {})",
                 saved.getId(), definition.getId(), definition.getProcessKey());

        return saved;
    }

    /**
     * Suspend a running process instance
     */
    @Transactional
    public ProcessInstance suspendProcess(Long instanceId, String reason) {
        log.debug("Suspending process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        if (instance.getStatus() != ProcessInstanceStatus.RUNNING) {
            throw new BusinessException(getMessage("instance.suspend.not-running"));
        }

        instance.setStatus(ProcessInstanceStatus.SUSPENDED);
        instance.setSuspensionReason(reason);
        ProcessInstance updated = instanceRepository.save(instance);

        recordHistory(updated, EventType.PROCESS_SUSPENDED, "Process suspended: " + reason);

        log.info("Suspended process instance {}", instanceId);
        return updated;
    }

    /**
     * Resume a suspended process instance
     */
    @Transactional
    public ProcessInstance resumeProcess(Long instanceId) {
        log.debug("Resuming process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        if (instance.getStatus() != ProcessInstanceStatus.SUSPENDED) {
            throw new BusinessException(getMessage("instance.resume.not-suspended"));
        }

        instance.setStatus(ProcessInstanceStatus.RUNNING);
        instance.setSuspensionReason(null);
        ProcessInstance updated = instanceRepository.save(instance);

        recordHistory(updated, EventType.PROCESS_RESUMED, "Process resumed");

        log.info("Resumed process instance {}", instanceId);
        return updated;
    }

    /**
     * Terminate a process instance
     */
    @Transactional
    public ProcessInstance terminateProcess(Long instanceId, String reason) {
        log.debug("Terminating process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        if (instance.isEnded()) {
            throw new BusinessException(getMessage("instance.terminate.already-ended"));
        }

        LocalDateTime endTime = LocalDateTime.now();
        instance.setStatus(ProcessInstanceStatus.TERMINATED);
        instance.setEndTime(endTime);
        instance.setTerminationReason(reason);
        instance.calculateDuration();

        ProcessInstance updated = instanceRepository.save(instance);

        recordHistory(updated, EventType.PROCESS_TERMINATED, "Process terminated: " + reason);

        log.info("Terminated process instance {}", instanceId);
        return updated;
    }

    /**
     * Mark process as completed
     */
    @Transactional
    public ProcessInstance completeProcess(Long instanceId) {
        log.debug("Completing process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        if (instance.getStatus() != ProcessInstanceStatus.RUNNING) {
            throw new BusinessException(getMessage("instance.complete.not-running"));
        }

        LocalDateTime endTime = LocalDateTime.now();
        instance.setStatus(ProcessInstanceStatus.COMPLETED);
        instance.setEndTime(endTime);
        instance.calculateDuration();

        ProcessInstance updated = instanceRepository.save(instance);

        recordHistory(updated, EventType.PROCESS_COMPLETED, "Process completed successfully");

        log.info("Completed process instance {}", instanceId);
        return updated;
    }

    /**
     * Mark process as failed
     */
    @Transactional
    public ProcessInstance failProcess(Long instanceId, String errorMessage, String stackTrace) {
        log.debug("Failing process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        LocalDateTime endTime = LocalDateTime.now();
        instance.setStatus(ProcessInstanceStatus.FAILED);
        instance.setEndTime(endTime);
        instance.setErrorMessage(errorMessage);
        instance.setErrorStackTrace(stackTrace);
        instance.calculateDuration();

        ProcessInstance updated = instanceRepository.save(instance);

        recordHistory(updated, EventType.PROCESS_FAILED, "Process failed: " + errorMessage);

        log.error("Failed process instance {}: {}", instanceId, errorMessage);
        return updated;
    }

    /**
     * Get all process instances
     */
    public Page<ProcessInstance> getAllInstances(Pageable pageable) {
        log.debug("Getting all process instances, page: {}", pageable.getPageNumber());
        return instanceRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get instance by ID
     */
    public ProcessInstance getInstanceById(Long id) {
        log.debug("Getting process instance by ID: {}", id);
        return findInstanceById(id);
    }

    /**
     * Get instances by status
     */
    public Page<ProcessInstance> getInstancesByStatus(ProcessInstanceStatus status, Pageable pageable) {
        log.debug("Getting process instances with status: {}", status);
        return instanceRepository.findByStatus(status, pageable);
    }

    /**
     * Get active instances
     */
    public Page<ProcessInstance> getActiveInstances(Pageable pageable) {
        log.debug("Getting active process instances");
        return instanceRepository.findActiveInstances(pageable);
    }

    /**
     * Get instances by process definition
     */
    public Page<ProcessInstance> getInstancesByProcessDefinition(Long definitionId, Pageable pageable) {
        log.debug("Getting instances for process definition: {}", definitionId);
        return instanceRepository.findByProcessDefinitionId(definitionId, pageable);
    }

    /**
     * Get instance by business key
     */
    public ProcessInstance getInstanceByBusinessKey(String businessKey) {
        log.debug("Getting instance by business key: {}", businessKey);
        return instanceRepository.findByBusinessKey(businessKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("instance.not-found-business-key", businessKey)));
    }

    /**
     * Get execution history for an instance
     */
    public List<ExecutionHistory> getInstanceHistory(Long instanceId) {
        log.debug("Getting execution history for instance: {}", instanceId);
        findInstanceById(instanceId); // Verify instance exists
        return historyRepository.findByProcessInstanceId(instanceId);
    }

    /**
     * Delete process instance (soft delete)
     */
    @Transactional
    public void deleteInstance(Long instanceId) {
        log.debug("Deleting process instance: {}", instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        if (instance.isActive()) {
            throw new BusinessException(getMessage("instance.delete.still-active"));
        }

        instance.setDeleted(true);
        instance.setDeletedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        log.info("Deleted process instance {}", instanceId);
    }

    /**
     * Record a history event
     */
    private void recordHistory(ProcessInstance instance, EventType eventType, String details) {
        ExecutionHistory history = ExecutionHistory.builder()
                .processInstance(instance)
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .eventDetails(details)
                .performedBy(getCurrentUsername())
                .build();

        historyRepository.save(history);
    }

    /**
     * Find process definition by ID or throw exception
     */
    private ProcessDefinition findDefinitionById(Long id) {
        return definitionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.not-found", id)));
    }

    /**
     * Find process instance by ID or throw exception
     */
    private ProcessInstance findInstanceById(Long id) {
        return instanceRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("instance.not-found", id)));
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * Get localized message
     */
    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
