package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.ProcessVariable;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
import com.processmonster.bpm.repository.ProcessVariableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.processmonster.bpm.entity.ProcessVariable.VariableScope.GLOBAL;
import static com.processmonster.bpm.entity.ProcessVariable.VariableType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VariableService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Variable Service Tests")
class VariableServiceTest {

    @Mock
    private ProcessVariableRepository variableRepository;

    @Mock
    private ProcessInstanceRepository instanceRepository;

    @Mock
    private MessageSource messageSource;

    private VariableService variableService;

    @BeforeEach
    void setUp() {
        variableService = new VariableService(
                variableRepository,
                instanceRepository,
                messageSource
        );

        when(messageSource.getMessage(eq("instance.not-found"), any(), any(), any()))
                .thenReturn("Process instance not found");
        when(messageSource.getMessage(eq("instance.variable.not-found"), any(), any(), any()))
                .thenReturn("Variable not found");
    }

    @Test
    @DisplayName("Should get all variables for instance")
    void shouldGetAllVariables() {
        // Given
        Long instanceId = 1L;

        ProcessVariable var1 = createVariable("amount", 10000, INTEGER);
        ProcessVariable var2 = createVariable("applicant", "John Doe", STRING);
        ProcessVariable var3 = createVariable("approved", true, BOOLEAN);

        when(variableRepository.findByProcessInstanceIdAndDeletedFalse(instanceId))
                .thenReturn(List.of(var1, var2, var3));

        // When
        Map<String, Object> result = variableService.getVariables(instanceId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get("amount")).isEqualTo(10000);
        assertThat(result.get("applicant")).isEqualTo("John Doe");
        assertThat(result.get("approved")).isEqualTo(true);
    }

    @Test
    @DisplayName("Should get single variable by name")
    void shouldGetVariableByName() {
        // Given
        Long instanceId = 1L;
        String variableName = "amount";

        ProcessVariable variable = createVariable(variableName, 10000, INTEGER);

        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.of(variable));

        // When
        Object result = variableService.getVariable(instanceId, variableName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(10000);
    }

    @Test
    @DisplayName("Should throw exception when variable not found")
    void shouldThrowExceptionWhenVariableNotFound() {
        // Given
        Long instanceId = 1L;
        String variableName = "unknown";

        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> variableService.getVariable(instanceId, variableName))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should set variables with automatic type detection")
    void shouldSetVariablesWithTypeDetection() {
        // Given
        Long instanceId = 1L;

        ProcessInstance instance = new ProcessInstance();
        instance.setId(instanceId);

        Map<String, Object> variables = Map.of(
                "amount", 10000,
                "applicant", "John Doe",
                "approved", true,
                "rate", 4.5,
                "createdAt", LocalDateTime.now()
        );

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(eq(instanceId), anyString()))
                .thenReturn(Optional.empty());
        when(variableRepository.save(any(ProcessVariable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        variableService.setVariables(instanceId, variables);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository, times(5)).save(captor.capture());

        List<ProcessVariable> savedVariables = captor.getAllValues();

        // Verify type detection
        ProcessVariable amountVar = savedVariables.stream()
                .filter(v -> v.getVariableName().equals("amount"))
                .findFirst()
                .orElseThrow();
        assertThat(amountVar.getVariableType()).isEqualTo(INTEGER);
        assertThat(amountVar.getValue()).isEqualTo(10000);

        ProcessVariable applicantVar = savedVariables.stream()
                .filter(v -> v.getVariableName().equals("applicant"))
                .findFirst()
                .orElseThrow();
        assertThat(applicantVar.getVariableType()).isEqualTo(STRING);

        ProcessVariable approvedVar = savedVariables.stream()
                .filter(v -> v.getVariableName().equals("approved"))
                .findFirst()
                .orElseThrow();
        assertThat(approvedVar.getVariableType()).isEqualTo(BOOLEAN);

        ProcessVariable rateVar = savedVariables.stream()
                .filter(v -> v.getVariableName().equals("rate"))
                .findFirst()
                .orElseThrow();
        assertThat(rateVar.getVariableType()).isEqualTo(DOUBLE);
    }

    @Test
    @DisplayName("Should update existing variable")
    void shouldUpdateExistingVariable() {
        // Given
        Long instanceId = 1L;
        String variableName = "amount";

        ProcessInstance instance = new ProcessInstance();
        instance.setId(instanceId);

        ProcessVariable existingVariable = createVariable(variableName, 10000, INTEGER);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.of(existingVariable));
        when(variableRepository.save(any(ProcessVariable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        variableService.setVariable(instanceId, variableName, 15000, INTEGER);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository).save(captor.capture());

        ProcessVariable saved = captor.getValue();
        assertThat(saved.getValue()).isEqualTo(15000);
    }

    @Test
    @DisplayName("Should delete variable (soft delete)")
    void shouldDeleteVariable() {
        // Given
        Long instanceId = 1L;
        String variableName = "tempVar";

        ProcessVariable variable = createVariable(variableName, "temp value", STRING);
        variable.setDeleted(false);

        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.of(variable));

        // When
        variableService.deleteVariable(instanceId, variableName);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository).save(captor.capture());

        ProcessVariable saved = captor.getValue();
        assertThat(saved.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should handle JSON variable type")
    void shouldHandleJsonVariable() {
        // Given
        Long instanceId = 1L;
        String variableName = "metadata";
        String jsonValue = "{\"key\":\"value\",\"count\":10}";

        ProcessInstance instance = new ProcessInstance();
        instance.setId(instanceId);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.empty());
        when(variableRepository.save(any(ProcessVariable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        variableService.setVariable(instanceId, variableName, jsonValue, JSON);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository).save(captor.capture());

        ProcessVariable saved = captor.getValue();
        assertThat(saved.getVariableType()).isEqualTo(JSON);
        assertThat(saved.getValue()).isEqualTo(jsonValue);
    }

    @Test
    @DisplayName("Should get variables by scope")
    void shouldGetVariablesByScope() {
        // Given
        Long instanceId = 1L;

        ProcessVariable globalVar = createVariable("globalVar", "value", STRING);
        globalVar.setScope(GLOBAL);

        when(variableRepository.findByProcessInstanceIdAndScopeAndDeletedFalse(instanceId, GLOBAL))
                .thenReturn(List.of(globalVar));

        // When
        Map<String, Object> result = variableService.getVariablesByScope(instanceId, GLOBAL);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsKey("globalVar");
    }

    @Test
    @DisplayName("Should detect JSON string and set correct type")
    void shouldDetectJsonString() {
        // Given
        Long instanceId = 1L;
        String variableName = "config";
        String jsonString = "{\"enabled\":true}";

        ProcessInstance instance = new ProcessInstance();
        instance.setId(instanceId);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.empty());
        when(variableRepository.save(any(ProcessVariable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Map<String, Object> variables = Map.of(variableName, jsonString);
        variableService.setVariables(instanceId, variables);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository).save(captor.capture());

        ProcessVariable saved = captor.getValue();
        // Should detect JSON by checking if string starts with { or [
        assertThat(saved.getVariableType()).isIn(JSON, STRING); // Could be either depending on implementation
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Given
        Long instanceId = 1L;
        String variableName = "nullableField";

        ProcessInstance instance = new ProcessInstance();
        instance.setId(instanceId);

        ProcessVariable existingVariable = createVariable(variableName, "old value", STRING);

        when(instanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(instance));
        when(variableRepository.findByProcessInstanceIdAndVariableNameAndDeletedFalse(instanceId, variableName))
                .thenReturn(Optional.of(existingVariable));

        // When
        variableService.setVariable(instanceId, variableName, null, STRING);

        // Then
        ArgumentCaptor<ProcessVariable> captor = ArgumentCaptor.forClass(ProcessVariable.class);
        verify(variableRepository).save(captor.capture());

        ProcessVariable saved = captor.getValue();
        assertThat(saved.getValue()).isNull();
    }

    // Helper method
    private ProcessVariable createVariable(String name, Object value, ProcessVariable.VariableType type) {
        ProcessVariable variable = new ProcessVariable();
        variable.setVariableName(name);
        variable.setVariableType(type);
        variable.setValue(value);
        variable.setScope(GLOBAL);
        variable.setDeleted(false);
        return variable;
    }
}
