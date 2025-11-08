package com.processmonster.bpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.TaskFormDataDTO;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FormTaskService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Form Task Service Tests")
class FormTaskServiceTest {

    @Mock
    private com.processmonster.bpm.service.TaskService taskService;

    @Mock
    private FormService formService;

    @Mock
    private FormValidationService validationService;

    @Mock
    private RuntimeService camundaRuntimeService;

    @Mock
    private TaskService camundaTaskService;

    @Mock
    private MessageSource messageSource;

    private FormTaskService formTaskService;
    private ObjectMapper objectMapper;

    private static final String VALID_FORM_SCHEMA = "{\"fields\":[{\"name\":\"loanAmount\",\"type\":\"number\",\"variableName\":\"loanAmount\"},{\"name\":\"duration\",\"type\":\"number\",\"variableName\":\"duration\"}]}";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        formTaskService = new FormTaskService(
                taskService,
                formService,
                validationService,
                camundaRuntimeService,
                camundaTaskService,
                objectMapper,
                messageSource
        );

        when(messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Test message");
    }

    // ========== getTaskForm Tests ==========

    @Test
    @DisplayName("Should get task form successfully with pre-filled values")
    void shouldGetTaskFormWithPrefilledValues() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("loanAmount", 50000);
        processVariables.put("duration", 24);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(camundaTaskService.getVariables("camunda-task-123")).thenReturn(processVariables);

        // When
        TaskFormDataDTO result = formTaskService.getTaskForm(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getFormKey()).isEqualTo("loan-application");
        assertThat(result.getFormDefinition()).isEqualTo(formDefinition);
        assertThat(result.getInitialValues()).containsEntry("loanAmount", 50000);
        assertThat(result.getInitialValues()).containsEntry("duration", 24);
        assertThat(result.isReadOnly()).isFalse();

        verify(taskService).getTaskById(taskId);
        verify(formService).getFormByKey("loan-application");
        verify(camundaTaskService).getVariables("camunda-task-123");
    }

    @Test
    @DisplayName("Should throw BusinessException when task has no formKey")
    void shouldThrowExceptionWhenTaskHasNoFormKey() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, null, "camunda-task-123");

        when(taskService.getTaskById(taskId)).thenReturn(task);

        // When/Then
        assertThatThrownBy(() -> formTaskService.getTaskForm(taskId))
                .isInstanceOf(BusinessException.class);

        verify(taskService).getTaskById(taskId);
        verify(formService, never()).getFormByKey(anyString());
    }

    @Test
    @DisplayName("Should throw BusinessException when form not found")
    void shouldThrowExceptionWhenFormNotFound() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "non-existent-form", "camunda-task-123");

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("non-existent-form"))
                .thenThrow(new ResourceNotFoundException("Form not found"));

        // When/Then
        assertThatThrownBy(() -> formTaskService.getTaskForm(taskId))
                .isInstanceOf(BusinessException.class);

        verify(taskService).getTaskById(taskId);
        verify(formService).getFormByKey("non-existent-form");
    }

    @Test
    @DisplayName("Should throw BusinessException when form is not published")
    void shouldThrowExceptionWhenFormNotPublished() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "draft-form", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("draft-form");
        formDefinition.setPublished(false);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("draft-form")).thenReturn(formDefinition);

        // When/Then
        assertThatThrownBy(() -> formTaskService.getTaskForm(taskId))
                .isInstanceOf(BusinessException.class);

        verify(taskService).getTaskById(taskId);
        verify(formService).getFormByKey("draft-form");
    }

    // ========== getTaskFormReadOnly Tests ==========

    @Test
    @DisplayName("Should get read-only form for completed task")
    void shouldGetReadOnlyFormForCompletedTask() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        task.setStatus(Task.TaskStatus.COMPLETED);
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("loanAmount", 50000);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(camundaTaskService.getVariables("camunda-task-123")).thenReturn(processVariables);

        // When
        TaskFormDataDTO result = formTaskService.getTaskFormReadOnly(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isReadOnly()).isTrue();
        assertThat(result.getFormKey()).isEqualTo("loan-application");
        assertThat(result.getInitialValues()).containsEntry("loanAmount", 50000);
    }

    @Test
    @DisplayName("Should return null when task has no formKey for read-only")
    void shouldReturnNullWhenTaskHasNoFormKeyForReadOnly() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, null, "camunda-task-123");

        when(taskService.getTaskById(taskId)).thenReturn(task);

        // When
        TaskFormDataDTO result = formTaskService.getTaskFormReadOnly(taskId);

        // Then
        assertThat(result).isNull();
        verify(taskService).getTaskById(taskId);
    }

    // ========== submitTaskForm Tests ==========

    @Test
    @DisplayName("Should submit task form successfully")
    void shouldSubmitTaskFormSuccessfully() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> formData = new HashMap<>();
        formData.put("loanAmount", 50000);
        formData.put("duration", 24);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(validationService.validateFormData(formDefinition, formData)).thenReturn(true);
        when(taskService.completeTask(eq(taskId), any())).thenReturn(task);

        // When
        Task result = formTaskService.submitTaskForm(taskId, formData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(task);

        verify(taskService).getTaskById(taskId);
        verify(formService).getFormByKey("loan-application");
        verify(validationService).validateFormData(formDefinition, formData);
        verify(camundaTaskService).setVariables(eq("camunda-task-123"), anyMap());
        verify(camundaTaskService).complete(eq("camunda-task-123"), anyMap());
        verify(taskService).completeTask(eq(taskId), any());
    }

    @Test
    @DisplayName("Should complete task without form when no formKey present")
    void shouldCompleteTaskWithoutFormWhenNoFormKey() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, null, "camunda-task-123");

        Map<String, Object> formData = new HashMap<>();

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(taskService.completeTask(taskId, formData)).thenReturn(task);

        // When
        Task result = formTaskService.submitTaskForm(taskId, formData);

        // Then
        assertThat(result).isNotNull();
        verify(taskService).completeTask(taskId, formData);
        verify(formService, never()).getFormByKey(anyString());
    }

    @Test
    @DisplayName("Should throw BusinessException when form validation fails")
    void shouldThrowExceptionWhenFormValidationFails() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> formData = new HashMap<>();
        formData.put("loanAmount", -1000); // Invalid data

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(validationService.validateFormData(formDefinition, formData)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> formTaskService.submitTaskForm(taskId, formData))
                .isInstanceOf(BusinessException.class);

        verify(validationService).validateFormData(formDefinition, formData);
        verify(camundaTaskService, never()).complete(anyString(), anyMap());
        verify(taskService, never()).completeTask(anyLong(), anyMap());
    }

    // ========== validateTaskFormData Tests ==========

    @Test
    @DisplayName("Should validate task form data successfully")
    void shouldValidateTaskFormDataSuccessfully() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> formData = new HashMap<>();
        formData.put("loanAmount", 50000);

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(validationService.validateFormData(formDefinition, formData)).thenReturn(true);

        // When
        boolean result = formTaskService.validateTaskFormData(taskId, formData);

        // Then
        assertThat(result).isTrue();
        verify(validationService).validateFormData(formDefinition, formData);
    }

    @Test
    @DisplayName("Should return true when task has no formKey for validation")
    void shouldReturnTrueWhenTaskHasNoFormKeyForValidation() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, null, "camunda-task-123");
        Map<String, Object> formData = new HashMap<>();

        when(taskService.getTaskById(taskId)).thenReturn(task);

        // When
        boolean result = formTaskService.validateTaskFormData(taskId, formData);

        // Then
        assertThat(result).isTrue();
        verify(formService, never()).getFormByKey(anyString());
    }

    @Test
    @DisplayName("Should return false when validation fails")
    void shouldReturnFalseWhenValidationFails() {
        // Given
        Long taskId = 1L;
        Task task = createTask(taskId, "loan-application", "camunda-task-123");
        FormDefinition formDefinition = createFormDefinition("loan-application");

        Map<String, Object> formData = new HashMap<>();

        when(taskService.getTaskById(taskId)).thenReturn(task);
        when(formService.getFormByKey("loan-application")).thenReturn(formDefinition);
        when(validationService.validateFormData(formDefinition, formData)).thenReturn(false);

        // When
        boolean result = formTaskService.validateTaskFormData(taskId, formData);

        // Then
        assertThat(result).isFalse();
    }

    // ========== Helper Methods ==========

    private Task createTask(Long id, String formKey, String camundaTaskId) {
        Task task = Task.builder()
                .id(id)
                .name("Test Task")
                .formKey(formKey)
                .camundaTaskId(camundaTaskId)
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();
        return task;
    }

    private FormDefinition createFormDefinition(String formKey) {
        FormDefinition formDefinition = FormDefinition.builder()
                .id(1L)
                .formKey(formKey)
                .name("Test Form")
                .schemaJson(VALID_FORM_SCHEMA)
                .published(true)
                .build();
        return formDefinition;
    }
}
