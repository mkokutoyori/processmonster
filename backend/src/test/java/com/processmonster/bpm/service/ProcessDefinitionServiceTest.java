package com.processmonster.bpm.service;

import com.processmonster.bpm.dto.definition.CreateProcessDefinitionDTO;
import com.processmonster.bpm.dto.definition.ProcessDefinitionDetailDTO;
import com.processmonster.bpm.dto.definition.UpdateProcessDefinitionDTO;
import com.processmonster.bpm.entity.ProcessCategory;
import com.processmonster.bpm.entity.ProcessDefinition;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.mapper.ProcessDefinitionMapper;
import com.processmonster.bpm.repository.ProcessCategoryRepository;
import com.processmonster.bpm.repository.ProcessDefinitionRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProcessDefinitionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Process Definition Service Tests")
class ProcessDefinitionServiceTest {

    @Mock
    private ProcessDefinitionRepository processRepository;

    @Mock
    private ProcessCategoryRepository categoryRepository;

    @Mock
    private BpmnValidationService bpmnValidationService;

    @Mock
    private ProcessDefinitionMapper processMapper;

    @Mock
    private MessageSource messageSource;

    private ProcessDefinitionService processService;

    private static final String VALID_BPMN_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         targetNamespace="http://processmonster.com/bpmn">
              <process id="loan-approval" name="Loan Approval" isExecutable="true">
                <startEvent id="startEvent1"/>
                <endEvent id="endEvent1"/>
              </process>
            </definitions>
            """;

    @BeforeEach
    void setUp() {
        processService = new ProcessDefinitionService(
                processRepository,
                categoryRepository,
                bpmnValidationService,
                processMapper,
                messageSource
        );

        // Setup default message source responses
        when(messageSource.getMessage(eq("process.not-found"), any(), any(), any()))
                .thenReturn("Process definition not found");
        when(messageSource.getMessage(eq("process.category.not-found"), any(), any(), any()))
                .thenReturn("Category not found");
    }

    @Test
    @DisplayName("Should create process definition successfully")
    void shouldCreateProcessDefinition() {
        // Given
        CreateProcessDefinitionDTO createDTO = CreateProcessDefinitionDTO.builder()
                .name("Loan Approval Process")
                .description("Process for approving loan applications")
                .bpmnXml(VALID_BPMN_XML)
                .categoryId(1L)
                .build();

        ProcessCategory category = new ProcessCategory();
        category.setId(1L);
        category.setCode("LOAN");
        category.setName("Loan Processes");

        ProcessDefinition savedProcess = new ProcessDefinition();
        savedProcess.setId(1L);
        savedProcess.setName("Loan Approval Process");
        savedProcess.setProcessKey("loan-approval");
        savedProcess.setVersion(1);
        savedProcess.setIsLatestVersion(true);
        savedProcess.setBpmnXml(VALID_BPMN_XML);

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(1L)
                .name("Loan Approval Process")
                .processKey("loan-approval")
                .version(1)
                .bpmnXml(VALID_BPMN_XML)
                .build();

        when(categoryRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(category));
        when(bpmnValidationService.extractProcessKey(VALID_BPMN_XML)).thenReturn("loan-approval");
        when(processRepository.findLatestVersionNumber("loan-approval")).thenReturn(0);
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(savedProcess);
        when(processMapper.toDetailDTO(savedProcess)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.createProcess(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Loan Approval Process");
        assertThat(result.getProcessKey()).isEqualTo("loan-approval");
        assertThat(result.getVersion()).isEqualTo(1);

        verify(bpmnValidationService).validateBpmnXml(VALID_BPMN_XML);
        verify(processRepository).save(any(ProcessDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        CreateProcessDefinitionDTO createDTO = CreateProcessDefinitionDTO.builder()
                .name("Test Process")
                .bpmnXml(VALID_BPMN_XML)
                .categoryId(999L)
                .build();

        when(categoryRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> processService.createProcess(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get process by ID successfully")
    void shouldGetProcessById() {
        // Given
        ProcessDefinition process = new ProcessDefinition();
        process.setId(1L);
        process.setName("Test Process");
        process.setProcessKey("test-process");

        when(processRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(process));

        // When
        ProcessDefinition result = processService.findProcessById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Process");
    }

    @Test
    @DisplayName("Should throw exception when process not found")
    void shouldThrowExceptionWhenProcessNotFound() {
        // Given
        when(processRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> processService.findProcessById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all processes with pagination")
    void shouldGetAllProcesses() {
        // Given
        ProcessDefinition process1 = new ProcessDefinition();
        process1.setId(1L);
        process1.setName("Process 1");

        ProcessDefinition process2 = new ProcessDefinition();
        process2.setId(2L);
        process2.setName("Process 2");

        Page<ProcessDefinition> processPage = new PageImpl<>(List.of(process1, process2));
        Pageable pageable = PageRequest.of(0, 10);

        when(processRepository.findByDeletedFalse(pageable)).thenReturn(processPage);

        // When
        Page<ProcessDefinition> result = processService.getAllProcesses(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Process 1");
    }

    @Test
    @DisplayName("Should create new version when BPMN XML changes")
    void shouldCreateNewVersionWhenBpmnChanges() {
        // Given
        Long processId = 1L;
        String newBpmnXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             targetNamespace="http://processmonster.com/bpmn">
                  <process id="loan-approval" name="Loan Approval V2" isExecutable="true">
                    <startEvent id="start"/>
                    <userTask id="task1" name="Review"/>
                    <endEvent id="end"/>
                  </process>
                </definitions>
                """;

        UpdateProcessDefinitionDTO updateDTO = UpdateProcessDefinitionDTO.builder()
                .name("Loan Approval V2")
                .bpmnXml(newBpmnXml)
                .build();

        ProcessDefinition existingProcess = new ProcessDefinition();
        existingProcess.setId(processId);
        existingProcess.setProcessKey("loan-approval");
        existingProcess.setVersion(1);
        existingProcess.setBpmnXml(VALID_BPMN_XML);

        ProcessDefinition newVersion = new ProcessDefinition();
        newVersion.setId(2L);
        newVersion.setProcessKey("loan-approval");
        newVersion.setVersion(2);
        newVersion.setIsLatestVersion(true);
        newVersion.setBpmnXml(newBpmnXml);

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(2L)
                .processKey("loan-approval")
                .version(2)
                .bpmnXml(newBpmnXml)
                .build();

        when(processRepository.findByIdAndDeletedFalse(processId)).thenReturn(Optional.of(existingProcess));
        when(bpmnValidationService.extractProcessKey(newBpmnXml)).thenReturn("loan-approval");
        when(processRepository.findLatestVersionNumber("loan-approval")).thenReturn(1);
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(newVersion);
        when(processMapper.toDetailDTO(newVersion)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.updateProcess(processId, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getBpmnXml()).isEqualTo(newBpmnXml);

        verify(processRepository).markAllVersionsAsNotLatest("loan-approval");
        verify(bpmnValidationService).validateBpmnXml(newBpmnXml);
    }

    @Test
    @DisplayName("Should update metadata without creating new version")
    void shouldUpdateMetadataWithoutNewVersion() {
        // Given
        Long processId = 1L;

        UpdateProcessDefinitionDTO updateDTO = UpdateProcessDefinitionDTO.builder()
                .name("Updated Process Name")
                .description("Updated description")
                .build();

        ProcessDefinition existingProcess = new ProcessDefinition();
        existingProcess.setId(processId);
        existingProcess.setProcessKey("loan-approval");
        existingProcess.setVersion(1);
        existingProcess.setBpmnXml(VALID_BPMN_XML);
        existingProcess.setName("Old Name");

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(processId)
                .name("Updated Process Name")
                .processKey("loan-approval")
                .version(1)
                .build();

        when(processRepository.findByIdAndDeletedFalse(processId)).thenReturn(Optional.of(existingProcess));
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(existingProcess);
        when(processMapper.toDetailDTO(existingProcess)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.updateProcess(processId, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Process Name");
        assertThat(result.getVersion()).isEqualTo(1); // Version unchanged

        ArgumentCaptor<ProcessDefinition> processCaptor = ArgumentCaptor.forClass(ProcessDefinition.class);
        verify(processRepository).save(processCaptor.capture());

        ProcessDefinition savedProcess = processCaptor.getValue();
        assertThat(savedProcess.getName()).isEqualTo("Updated Process Name");
        assertThat(savedProcess.getDescription()).isEqualTo("Updated description");

        verify(processRepository, never()).markAllVersionsAsNotLatest(anyString());
    }

    @Test
    @DisplayName("Should publish process")
    void shouldPublishProcess() {
        // Given
        Long processId = 1L;

        ProcessDefinition process = new ProcessDefinition();
        process.setId(processId);
        process.setPublished(false);

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(processId)
                .published(true)
                .build();

        when(processRepository.findByIdAndDeletedFalse(processId)).thenReturn(Optional.of(process));
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(process);
        when(processMapper.toDetailDTO(process)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.publishProcess(processId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPublished()).isTrue();

        ArgumentCaptor<ProcessDefinition> captor = ArgumentCaptor.forClass(ProcessDefinition.class);
        verify(processRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isTrue();
    }

    @Test
    @DisplayName("Should unpublish process")
    void shouldUnpublishProcess() {
        // Given
        Long processId = 1L;

        ProcessDefinition process = new ProcessDefinition();
        process.setId(processId);
        process.setPublished(true);

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(processId)
                .published(false)
                .build();

        when(processRepository.findByIdAndDeletedFalse(processId)).thenReturn(Optional.of(process));
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(process);
        when(processMapper.toDetailDTO(process)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.unpublishProcess(processId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPublished()).isFalse();
    }

    @Test
    @DisplayName("Should delete process (soft delete)")
    void shouldDeleteProcess() {
        // Given
        Long processId = 1L;

        ProcessDefinition process = new ProcessDefinition();
        process.setId(processId);
        process.setDeleted(false);

        when(processRepository.findByIdAndDeletedFalse(processId)).thenReturn(Optional.of(process));

        // When
        processService.deleteProcess(processId);

        // Then
        ArgumentCaptor<ProcessDefinition> captor = ArgumentCaptor.forClass(ProcessDefinition.class);
        verify(processRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should search processes by keyword")
    void shouldSearchProcesses() {
        // Given
        String keyword = "loan";
        Pageable pageable = PageRequest.of(0, 10);

        ProcessDefinition process1 = new ProcessDefinition();
        process1.setId(1L);
        process1.setName("Loan Approval");

        Page<ProcessDefinition> processPage = new PageImpl<>(List.of(process1));

        when(processRepository.searchProcesses(keyword, pageable)).thenReturn(processPage);

        // When
        Page<ProcessDefinition> result = processService.searchProcesses(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("Loan");
    }

    @Test
    @DisplayName("Should get processes by category")
    void shouldGetProcessesByCategory() {
        // Given
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        ProcessDefinition process1 = new ProcessDefinition();
        process1.setId(1L);

        Page<ProcessDefinition> processPage = new PageImpl<>(List.of(process1));

        when(processRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable)).thenReturn(processPage);

        // When
        Page<ProcessDefinition> result = processService.getProcessesByCategory(categoryId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should import BPMN process successfully")
    void shouldImportBpmnProcess() {
        // Given
        ProcessDefinition savedProcess = new ProcessDefinition();
        savedProcess.setId(1L);
        savedProcess.setProcessKey("loan-approval");
        savedProcess.setVersion(1);

        ProcessDefinitionDetailDTO resultDTO = ProcessDefinitionDetailDTO.builder()
                .id(1L)
                .processKey("loan-approval")
                .version(1)
                .build();

        when(bpmnValidationService.extractProcessKey(VALID_BPMN_XML)).thenReturn("loan-approval");
        when(bpmnValidationService.extractProcessName(VALID_BPMN_XML)).thenReturn("Loan Approval");
        when(processRepository.findLatestVersionNumber("loan-approval")).thenReturn(0);
        when(processRepository.save(any(ProcessDefinition.class))).thenReturn(savedProcess);
        when(processMapper.toDetailDTO(savedProcess)).thenReturn(resultDTO);

        // When
        ProcessDefinitionDetailDTO result = processService.importBpmnProcess(VALID_BPMN_XML, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProcessKey()).isEqualTo("loan-approval");

        verify(bpmnValidationService).validateBpmnXml(VALID_BPMN_XML);
        verify(bpmnValidationService).extractProcessKey(VALID_BPMN_XML);
        verify(bpmnValidationService).extractProcessName(VALID_BPMN_XML);
    }
}
