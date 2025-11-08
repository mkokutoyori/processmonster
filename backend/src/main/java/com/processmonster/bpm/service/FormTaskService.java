package com.processmonster.bpm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.TaskFormDataDTO;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for integrating forms with tasks
 * Handles form retrieval, validation, and variable mapping
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormTaskService {

    private final com.processmonster.bpm.service.TaskService taskService;
    private final FormService formService;
    private final FormValidationService validationService;
    private final RuntimeService camundaRuntimeService;
    private final TaskService camundaTaskService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    /**
     * Get form associated with a task
     * Returns the form definition and pre-fills it with process variables
     */
    public TaskFormDataDTO getTaskForm(Long taskId) {
        Task task = taskService.getTaskById(taskId);

        // Check if task has a form
        if (task.getFormKey() == null || task.getFormKey().isEmpty()) {
            throw new BusinessException(getMessage("task.form.not-configured", task.getName()));
        }

        // Get form definition by formKey
        FormDefinition formDefinition;
        try {
            formDefinition = formService.getFormByKey(task.getFormKey());
        } catch (ResourceNotFoundException e) {
            throw new BusinessException(getMessage("task.form.not-found", task.getFormKey()));
        }

        // Check if form is published
        if (!formDefinition.isActive()) {
            throw new BusinessException(getMessage("task.form.not-published", task.getFormKey()));
        }

        // Get process variables from Camunda
        Map<String, Object> processVariables = getProcessVariables(task);

        // Pre-fill form with existing variables
        Map<String, Object> initialValues = mapVariablesToFormFields(
            formDefinition,
            processVariables
        );

        return TaskFormDataDTO.builder()
            .taskId(taskId)
            .taskName(task.getName())
            .formKey(task.getFormKey())
            .formDefinition(formDefinition)
            .initialValues(initialValues)
            .processVariables(processVariables)
            .readOnly(false)
            .build();
    }

    /**
     * Get form for a completed task (read-only)
     */
    public TaskFormDataDTO getTaskFormReadOnly(Long taskId) {
        Task task = taskService.getTaskById(taskId);

        if (task.getFormKey() == null || task.getFormKey().isEmpty()) {
            return null;
        }

        FormDefinition formDefinition;
        try {
            formDefinition = formService.getFormByKey(task.getFormKey());
        } catch (ResourceNotFoundException e) {
            log.warn("Form definition not found for completed task: {}", task.getFormKey());
            return null;
        }

        // Get historical process variables
        Map<String, Object> processVariables = getProcessVariables(task);
        Map<String, Object> submittedValues = mapVariablesToFormFields(
            formDefinition,
            processVariables
        );

        return TaskFormDataDTO.builder()
            .taskId(taskId)
            .taskName(task.getName())
            .formKey(task.getFormKey())
            .formDefinition(formDefinition)
            .initialValues(submittedValues)
            .processVariables(processVariables)
            .readOnly(true)
            .build();
    }

    /**
     * Submit form and complete task
     * Validates form data and maps it to process variables
     */
    @Transactional
    public Task submitTaskForm(Long taskId, Map<String, Object> formData) {
        Task task = taskService.getTaskById(taskId);

        // Check if task has a form
        if (task.getFormKey() == null || task.getFormKey().isEmpty()) {
            // No form required, just complete the task
            return taskService.completeTask(taskId, formData);
        }

        // Get form definition
        FormDefinition formDefinition;
        try {
            formDefinition = formService.getFormByKey(task.getFormKey());
        } catch (ResourceNotFoundException e) {
            throw new BusinessException(getMessage("task.form.not-found", task.getFormKey()));
        }

        // Validate form data against schema
        boolean isValid = validationService.validateFormData(formDefinition, formData);
        if (!isValid) {
            throw new BusinessException(getMessage("task.form.validation-failed"));
        }

        // Map form data to process variables
        Map<String, Object> processVariables = mapFormFieldsToVariables(formDefinition, formData);

        // Set variables in Camunda process
        if (task.getCamundaTaskId() != null) {
            setProcessVariables(task, processVariables);
        }

        // Complete the task in Camunda
        if (task.getCamundaTaskId() != null) {
            try {
                camundaTaskService.complete(task.getCamundaTaskId(), processVariables);
                log.info("Camunda task completed: {} with {} variables",
                    task.getCamundaTaskId(), processVariables.size());
            } catch (Exception e) {
                log.error("Error completing Camunda task: {}", task.getCamundaTaskId(), e);
                throw new BusinessException(getMessage("task.camunda.complete-failed"));
            }
        }

        // Complete the task in ProcessMonster
        Task completedTask = taskService.completeTask(taskId, formData);
        log.info("Task completed with form: {} (Form Key: {})", taskId, task.getFormKey());

        return completedTask;
    }

    /**
     * Validate form data without submitting
     */
    public boolean validateTaskFormData(Long taskId, Map<String, Object> formData) {
        Task task = taskService.getTaskById(taskId);

        if (task.getFormKey() == null || task.getFormKey().isEmpty()) {
            return true; // No form to validate
        }

        FormDefinition formDefinition;
        try {
            formDefinition = formService.getFormByKey(task.getFormKey());
        } catch (ResourceNotFoundException e) {
            return false;
        }

        return validationService.validateFormData(formDefinition, formData);
    }

    /**
     * Get process variables from Camunda
     */
    private Map<String, Object> getProcessVariables(Task task) {
        if (task.getCamundaTaskId() == null) {
            return new HashMap<>();
        }

        try {
            // Get variables from Camunda task
            return camundaTaskService.getVariables(task.getCamundaTaskId());
        } catch (Exception e) {
            log.warn("Could not retrieve Camunda variables for task: {}", task.getCamundaTaskId(), e);
            return new HashMap<>();
        }
    }

    /**
     * Set process variables in Camunda
     */
    private void setProcessVariables(Task task, Map<String, Object> variables) {
        if (task.getCamundaTaskId() == null) {
            return;
        }

        try {
            camundaTaskService.setVariables(task.getCamundaTaskId(), variables);
            log.info("Set {} variables in Camunda task: {}",
                variables.size(), task.getCamundaTaskId());
        } catch (Exception e) {
            log.error("Error setting Camunda variables for task: {}", task.getCamundaTaskId(), e);
            throw new BusinessException(getMessage("task.camunda.set-variables-failed"));
        }
    }

    /**
     * Map process variables to form field initial values
     * Extracts values from process variables that match form field names
     */
    private Map<String, Object> mapVariablesToFormFields(
            FormDefinition formDefinition,
            Map<String, Object> processVariables) {

        Map<String, Object> initialValues = new HashMap<>();

        try {
            // Parse the form schema to get field names
            JsonNode schemaNode = objectMapper.readTree(formDefinition.getSchemaJson());
            JsonNode fieldsNode = schemaNode.get("fields");

            if (fieldsNode != null && fieldsNode.isArray()) {
                for (JsonNode fieldNode : fieldsNode) {
                    String fieldName = fieldNode.get("name").asText();
                    String variableName = fieldNode.has("variableName")
                        ? fieldNode.get("variableName").asText()
                        : fieldName;

                    // If process variable exists, use it as initial value
                    if (processVariables.containsKey(variableName)) {
                        initialValues.put(fieldName, processVariables.get(variableName));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing form schema for pre-fill: {}", formDefinition.getFormKey(), e);
        }

        return initialValues;
    }

    /**
     * Map form field values to process variables
     * Converts form data to Camunda process variables
     */
    private Map<String, Object> mapFormFieldsToVariables(
            FormDefinition formDefinition,
            Map<String, Object> formData) {

        Map<String, Object> variables = new HashMap<>();

        try {
            // Parse the form schema to get variable mappings
            JsonNode schemaNode = objectMapper.readTree(formDefinition.getSchemaJson());
            JsonNode fieldsNode = schemaNode.get("fields");

            if (fieldsNode != null && fieldsNode.isArray()) {
                for (JsonNode fieldNode : fieldsNode) {
                    String fieldName = fieldNode.get("name").asText();
                    String variableName = fieldNode.has("variableName")
                        ? fieldNode.get("variableName").asText()
                        : fieldName;

                    // Map field value to variable
                    if (formData.containsKey(fieldName)) {
                        variables.put(variableName, formData.get(fieldName));
                    }
                }
            }

            // Also include any extra form data as variables
            formData.forEach((key, value) -> {
                if (!variables.containsKey(key)) {
                    variables.put(key, value);
                }
            });

        } catch (Exception e) {
            log.warn("Error parsing form schema for variable mapping: {}",
                formDefinition.getFormKey(), e);
            // Fallback: use form data as-is
            variables.putAll(formData);
        }

        return variables;
    }

    /**
     * Get localized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
