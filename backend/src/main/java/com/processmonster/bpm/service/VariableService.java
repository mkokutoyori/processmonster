package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.ProcessVariable;
import com.processmonster.bpm.entity.ProcessVariable.VariableScope;
import com.processmonster.bpm.entity.ProcessVariable.VariableType;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
import com.processmonster.bpm.repository.ProcessVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing process variables
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VariableService {

    private final ProcessVariableRepository variableRepository;
    private final ProcessInstanceRepository instanceRepository;
    private final MessageSource messageSource;

    /**
     * Get all variables for a process instance
     */
    public Map<String, Object> getVariables(Long instanceId) {
        log.debug("Getting all variables for instance: {}", instanceId);
        List<ProcessVariable> variables = variableRepository.findByProcessInstanceId(instanceId);

        return variables.stream()
                .collect(Collectors.toMap(
                        ProcessVariable::getVariableName,
                        ProcessVariable::getValue
                ));
    }

    /**
     * Get a specific variable value
     */
    public Object getVariable(Long instanceId, String variableName) {
        log.debug("Getting variable '{}' for instance: {}", variableName, instanceId);

        ProcessVariable variable = variableRepository.findByInstanceIdAndName(instanceId, variableName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("instance.variable.not-found", variableName)));

        return variable.getValue();
    }

    /**
     * Set a variable value (creates or updates)
     */
    @Transactional
    public void setVariable(Long instanceId, String variableName, Object value, VariableType type) {
        log.debug("Setting variable '{}' for instance: {}", variableName, instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        ProcessVariable variable = variableRepository.findByInstanceIdAndName(instanceId, variableName)
                .orElseGet(() -> {
                    ProcessVariable newVar = new ProcessVariable();
                    newVar.setProcessInstance(instance);
                    newVar.setVariableName(variableName);
                    newVar.setVariableType(type);
                    newVar.setScope(VariableScope.GLOBAL);
                    newVar.setIsTransient(false);
                    return newVar;
                });

        variable.setValue(value);
        variableRepository.save(variable);

        log.info("Set variable '{}' for instance {}", variableName, instanceId);
    }

    /**
     * Set multiple variables at once
     */
    @Transactional
    public void setVariables(Long instanceId, Map<String, Object> variables) {
        log.debug("Setting {} variables for instance: {}", variables.size(), instanceId);

        ProcessInstance instance = findInstanceById(instanceId);

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            VariableType type = detectVariableType(entry.getValue());
            setVariable(instanceId, entry.getKey(), entry.getValue(), type);
        }
    }

    /**
     * Delete a variable
     */
    @Transactional
    public void deleteVariable(Long instanceId, String variableName) {
        log.debug("Deleting variable '{}' for instance: {}", variableName, instanceId);

        if (!variableRepository.existsByInstanceIdAndName(instanceId, variableName)) {
            throw new ResourceNotFoundException(
                    getMessage("instance.variable.not-found", variableName));
        }

        variableRepository.deleteByInstanceIdAndName(instanceId, variableName);
        log.info("Deleted variable '{}' for instance {}", variableName, instanceId);
    }

    /**
     * Check if variable exists
     */
    public boolean hasVariable(Long instanceId, String variableName) {
        return variableRepository.existsByInstanceIdAndName(instanceId, variableName);
    }

    /**
     * Get variables by scope
     */
    public Map<String, Object> getVariablesByScope(Long instanceId, VariableScope scope) {
        log.debug("Getting {} variables for instance: {}", scope, instanceId);

        List<ProcessVariable> variables = variableRepository.findByInstanceIdAndScope(instanceId, scope);

        return variables.stream()
                .collect(Collectors.toMap(
                        ProcessVariable::getVariableName,
                        ProcessVariable::getValue
                ));
    }

    /**
     * Get non-transient variables (for persistence)
     */
    public Map<String, Object> getNonTransientVariables(Long instanceId) {
        log.debug("Getting non-transient variables for instance: {}", instanceId);

        List<ProcessVariable> variables = variableRepository.findNonTransientVariables(instanceId);

        return variables.stream()
                .collect(Collectors.toMap(
                        ProcessVariable::getVariableName,
                        ProcessVariable::getValue
                ));
    }

    /**
     * Delete all transient variables
     */
    @Transactional
    public void deleteTransientVariables(Long instanceId) {
        log.debug("Deleting transient variables for instance: {}", instanceId);
        variableRepository.deleteTransientVariables(instanceId);
    }

    /**
     * Detect variable type from value
     */
    private VariableType detectVariableType(Object value) {
        if (value == null) {
            return VariableType.STRING;
        }

        if (value instanceof Boolean) {
            return VariableType.BOOLEAN;
        } else if (value instanceof Integer || value instanceof Long) {
            return VariableType.INTEGER;
        } else if (value instanceof Double || value instanceof Float) {
            return VariableType.DOUBLE;
        } else if (value instanceof LocalDateTime) {
            return VariableType.DATE;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.startsWith("{") || str.startsWith("[")) {
                return VariableType.JSON;
            }
            return VariableType.STRING;
        }

        return VariableType.STRING;
    }

    /**
     * Find instance by ID or throw exception
     */
    private ProcessInstance findInstanceById(Long id) {
        return instanceRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("instance.not-found", id)));
    }

    /**
     * Get localized message
     */
    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
