package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ExecutionHistory;
import com.processmonster.bpm.entity.ProcessDefinition;
import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.ExecutionHistoryRepository;
import com.processmonster.bpm.repository.ProcessDefinitionRepository;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.processmonster.bpm.entity.ExecutionHistory.EventType.*;
import static com.processmonster.bpm.entity.ProcessInstance.ProcessInstanceStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProcessExecutionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Process Execution Service Tests")
class ProcessExecutionServiceTest {

    @Mock
    private ProcessInstanceRepository instanceRepository;

    @Mock
    private ProcessDefinitionRepository definitionRepository;

    @Mock
    private ExecutionHistoryRepository historyRepository;

    @Mock
    private VariableService variableService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private ProcessExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new ProcessExecutionService(
                instanceRepository,
                definitionRepository,
                historyRepository,
                variableService,
                messageSource
        );

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        // Setup default message source responses
        when(messageSource.getMessage(eq("instance.not-found"), any(), any(), any()))
                .thenReturn("Process instance not found");
        when(messageSource.getMessage(eq("process.not-found"), any(), any(), any()))
                .thenReturn("Process definition not found");
        when(messageSource.getMessage(eq("instance.start.not-published"), any(), any(), any()))
                .thenReturn("Cannot start: process is not published");
        when(messageSource.getMessage(eq("instance.suspend.not-running"), any(), any(), any()))
                .thenReturn("Cannot suspend: instance is not running");
        when(messageSource.getMessage(eq("instance.resume.not-suspended"), any(), any(), any()))
                .thenReturn("Cannot resume: instance is not suspended");
    }

    @Test
    @DisplayName("Should start process successfully")
    void shouldStartProcess() {
        // Given
        Long definitionId = 1L;
        String businessKey = "LOAN-12345";
        Map<String, Object> variables = Map.of("amount", 10000, "applicant", "John Doe");

        ProcessDefinition definition = createProcessDefinition();
        definition.setPublished(true);
        definition.setActive(true);

        ProcessInstance savedInstance = new ProcessInstance();
        savedInstance.setId(1L);
        savedInstance.setStatus(RUNNING);
        savedInstance.setProcessDefinition(definition);

        when(definitionRepository.findByIdAndDeletedFalse(definitionId)).thenReturn(Optional.of(definition));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(savedInstance);

        // When
        ProcessInstance result = executionService.startProcess(definitionId, businessKey, variables);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RUNNING);

        ArgumentCaptor<ProcessInstance> instanceCaptor = ArgumentCaptor.forClass(ProcessInstance.class);
        verify(instanceRepository).save(instanceCaptor.capture());

        ProcessInstance capturedInstance = instanceCaptor.getValue();
        assertThat(capturedInstance.getBusinessKey()).isEqualTo(businessKey);
        assertThat(capturedInstance.getStartedBy()).isEqualTo("testuser");
        assertThat(capturedInstance.getStartTime()).isNotNull();

        verify(variableService).setVariables(1L, variables);
        verify(historyRepository).save(any(ExecutionHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when starting unpublished process")
    void shouldThrowExceptionWhenStartingUnpublishedProcess() {
        // Given
        Long definitionId = 1L;

        ProcessDefinition definition = createProcessDefinition();
        definition.setPublished(false);

        when(definitionRepository.findByIdAndDeletedFalse(definitionId)).thenReturn(Optional.of(definition));

        // When / Then
        assertThatThrownBy(() -> executionService.startProcess(definitionId, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not published");
    }

    @Test
    @DisplayName("Should throw exception when starting inactive process")
    void shouldThrowExceptionWhenStartingInactiveProcess() {
        // Given
        Long definitionId = 1L;

        ProcessDefinition definition = createProcessDefinition();
        definition.setPublished(true);
        definition.setActive(false);

        when(definitionRepository.findByIdAndDeletedFalse(definitionId)).thenReturn(Optional.of(definition));
        when(messageSource.getMessage(eq("instance.start.not-active"), any(), any(), any()))
                .thenReturn("Cannot start: process is not active");

        // When / Then
        assertThatThrownBy(() -> executionService.startProcess(definitionId, null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should suspend running instance")
    void shouldSuspendRunningInstance() {
        // Given
        Long instanceId = 1L;
        String reason = "Pending additional documentation";

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        ProcessInstance result = executionService.suspendProcess(instanceId, reason);

        // Then
        assertThat(result.getStatus()).isEqualTo(SUSPENDED);

        ArgumentCaptor<ProcessInstance> captor = ArgumentCaptor.forClass(ProcessInstance.class);
        verify(instanceRepository).save(captor.capture());

        ProcessInstance saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SUSPENDED);
        assertThat(saved.getSuspensionReason()).isEqualTo(reason);

        verify(historyRepository).save(argThat(history ->
                history.getEventType() == PROCESS_SUSPENDED &&
                history.getEventDetails().contains(reason)
        ));
    }

    @Test
    @DisplayName("Should throw exception when suspending non-running instance")
    void shouldThrowExceptionWhenSuspendingNonRunningInstance() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(COMPLETED);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));

        // When / Then
        assertThatThrownBy(() -> executionService.suspendProcess(instanceId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not running");
    }

    @Test
    @DisplayName("Should resume suspended instance")
    void shouldResumeSuspendedInstance() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(SUSPENDED);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        ProcessInstance result = executionService.resumeProcess(instanceId);

        // Then
        assertThat(result.getStatus()).isEqualTo(RUNNING);

        ArgumentCaptor<ProcessInstance> captor = ArgumentCaptor.forClass(ProcessInstance.class);
        verify(instanceRepository).save(captor.capture());

        ProcessInstance saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(RUNNING);
        assertThat(saved.getSuspensionReason()).isNull();

        verify(historyRepository).save(argThat(history ->
                history.getEventType() == PROCESS_RESUMED
        ));
    }

    @Test
    @DisplayName("Should throw exception when resuming non-suspended instance")
    void shouldThrowExceptionWhenResumingNonSuspendedInstance() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));

        // When / Then
        assertThatThrownBy(() -> executionService.resumeProcess(instanceId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not suspended");
    }

    @Test
    @DisplayName("Should terminate instance")
    void shouldTerminateInstance() {
        // Given
        Long instanceId = 1L;
        String reason = "Business requirements changed";

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        ProcessInstance result = executionService.terminateProcess(instanceId, reason);

        // Then
        assertThat(result.getStatus()).isEqualTo(TERMINATED);
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getDurationMillis()).isNotNull();

        verify(historyRepository).save(argThat(history ->
                history.getEventType() == PROCESS_TERMINATED &&
                history.getEventDetails().contains(reason)
        ));
    }

    @Test
    @DisplayName("Should complete process successfully")
    void shouldCompleteProcess() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        ProcessInstance result = executionService.completeProcess(instanceId);

        // Then
        assertThat(result.getStatus()).isEqualTo(COMPLETED);
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getDurationMillis()).isNotNull();

        verify(historyRepository).save(argThat(history ->
                history.getEventType() == PROCESS_COMPLETED
        ));
    }

    @Test
    @DisplayName("Should fail process with error message")
    void shouldFailProcess() {
        // Given
        Long instanceId = 1L;
        String errorMessage = "Database connection failed";

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        ProcessInstance result = executionService.failProcess(instanceId, errorMessage, null);

        // Then
        assertThat(result.getStatus()).isEqualTo(FAILED);
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(result.getEndTime()).isNotNull();

        verify(historyRepository).save(argThat(history ->
                history.getEventType() == PROCESS_FAILED &&
                history.getErrorMessage().equals(errorMessage)
        ));
    }

    @Test
    @DisplayName("Should get all instances with pagination")
    void shouldGetAllInstances() {
        // Given
        ProcessInstance instance1 = createProcessInstance();
        instance1.setId(1L);

        ProcessInstance instance2 = createProcessInstance();
        instance2.setId(2L);

        Page<ProcessInstance> instancePage = new PageImpl<>(List.of(instance1, instance2));
        Pageable pageable = PageRequest.of(0, 10);

        when(instanceRepository.findByDeletedFalse(pageable)).thenReturn(instancePage);

        // When
        Page<ProcessInstance> result = executionService.getAllInstances(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should get instances by status")
    void shouldGetInstancesByStatus() {
        // Given
        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        Page<ProcessInstance> instancePage = new PageImpl<>(List.of(instance));
        Pageable pageable = PageRequest.of(0, 10);

        when(instanceRepository.findByStatusAndDeletedFalse(RUNNING, pageable)).thenReturn(instancePage);

        // When
        Page<ProcessInstance> result = executionService.getInstancesByStatus(RUNNING, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RUNNING);
    }

    @Test
    @DisplayName("Should get active instances")
    void shouldGetActiveInstances() {
        // Given
        ProcessInstance running = createProcessInstance();
        running.setStatus(RUNNING);

        ProcessInstance suspended = createProcessInstance();
        suspended.setStatus(SUSPENDED);

        Page<ProcessInstance> instancePage = new PageImpl<>(List.of(running, suspended));
        Pageable pageable = PageRequest.of(0, 10);

        when(instanceRepository.findActiveInstances(pageable)).thenReturn(instancePage);

        // When
        Page<ProcessInstance> result = executionService.getActiveInstances(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should get instance history")
    void shouldGetInstanceHistory() {
        // Given
        Long instanceId = 1L;

        ExecutionHistory event1 = new ExecutionHistory();
        event1.setEventType(PROCESS_STARTED);
        event1.setTimestamp(LocalDateTime.now().minusHours(2));

        ExecutionHistory event2 = new ExecutionHistory();
        event2.setEventType(ACTIVITY_STARTED);
        event2.setTimestamp(LocalDateTime.now().minusHours(1));

        List<ExecutionHistory> history = List.of(event1, event2);

        when(historyRepository.findByProcessInstanceIdOrderByTimestampAsc(instanceId)).thenReturn(history);

        // When
        List<ExecutionHistory> result = executionService.getInstanceHistory(instanceId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEventType()).isEqualTo(PROCESS_STARTED);
        assertThat(result.get(1).getEventType()).isEqualTo(ACTIVITY_STARTED);
    }

    @Test
    @DisplayName("Should delete instance only if not active")
    void shouldDeleteInstanceOnlyIfNotActive() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(COMPLETED);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));

        // When
        executionService.deleteInstance(instanceId);

        // Then
        ArgumentCaptor<ProcessInstance> captor = ArgumentCaptor.forClass(ProcessInstance.class);
        verify(instanceRepository).save(captor.capture());

        ProcessInstance saved = captor.getValue();
        assertThat(saved.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should not delete active instance")
    void shouldNotDeleteActiveInstance() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = createProcessInstance();
        instance.setStatus(RUNNING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(messageSource.getMessage(eq("instance.delete.still-active"), any(), any(), any()))
                .thenReturn("Cannot delete active instance");

        // When / Then
        assertThatThrownBy(() -> executionService.deleteInstance(instanceId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("active");
    }

    // Helper methods
    private ProcessDefinition createProcessDefinition() {
        ProcessDefinition definition = new ProcessDefinition();
        definition.setId(1L);
        definition.setProcessKey("test-process");
        definition.setName("Test Process");
        definition.setVersion(1);
        definition.setPublished(true);
        definition.setActive(true);
        return definition;
    }

    private ProcessInstance createProcessInstance() {
        ProcessInstance instance = new ProcessInstance();
        instance.setId(1L);
        instance.setProcessDefinition(createProcessDefinition());
        instance.setStatus(RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setStartedBy("testuser");
        return instance;
    }
}
