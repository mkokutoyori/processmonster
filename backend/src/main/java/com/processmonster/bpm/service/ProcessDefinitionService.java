package com.processmonster.bpm.service;

import com.processmonster.bpm.dto.process.CreateProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDetailDTO;
import com.processmonster.bpm.dto.process.ProcessVersionInfoDTO;
import com.processmonster.bpm.dto.process.UpdateProcessDefinitionDTO;
import com.processmonster.bpm.entity.ProcessCategory;
import com.processmonster.bpm.entity.ProcessDefinition;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.mapper.ProcessDefinitionMapper;
import com.processmonster.bpm.repository.ProcessCategoryRepository;
import com.processmonster.bpm.repository.ProcessDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing process definitions with versioning support
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessDefinitionService {

    private final ProcessDefinitionRepository processRepository;
    private final ProcessCategoryRepository categoryRepository;
    private final ProcessDefinitionMapper processMapper;
    private final BpmnValidationService bpmnValidationService;
    private final MessageSource messageSource;

    /**
     * Get all process definitions (latest versions only)
     */
    public Page<ProcessDefinitionDTO> getAllProcesses(Pageable pageable) {
        log.debug("Getting all processes (latest versions), page: {}", pageable.getPageNumber());
        return processRepository.findByDeletedFalseAndIsLatestVersionTrue(pageable)
                .map(processMapper::toDTO);
    }

    /**
     * Get all process definitions including all versions
     */
    public Page<ProcessDefinitionDTO> getAllProcessesAllVersions(Pageable pageable) {
        log.debug("Getting all processes (all versions), page: {}", pageable.getPageNumber());
        return processRepository.findByDeletedFalse(pageable)
                .map(processMapper::toDTO);
    }

    /**
     * Get process by ID (with BPMN XML)
     */
    public ProcessDefinitionDetailDTO getProcessById(Long id) {
        log.debug("Getting process by ID: {}", id);
        ProcessDefinition process = findProcessById(id);
        return processMapper.toDetailDTO(process);
    }

    /**
     * Get latest version of a process by key
     */
    public ProcessDefinitionDetailDTO getLatestVersionByKey(String processKey) {
        log.debug("Getting latest version of process: {}", processKey);
        ProcessDefinition process = processRepository.findLatestVersionByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.not-found-key", processKey)));
        return processMapper.toDetailDTO(process);
    }

    /**
     * Get specific version of a process
     */
    public ProcessDefinitionDetailDTO getProcessByKeyAndVersion(String processKey, Integer version) {
        log.debug("Getting process {}:v{}", processKey, version);
        ProcessDefinition process = processRepository.findByProcessKeyAndVersion(processKey, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.not-found-version", processKey, version)));
        return processMapper.toDetailDTO(process);
    }

    /**
     * Get all versions of a process
     */
    public List<ProcessVersionInfoDTO> getAllVersionsByKey(String processKey) {
        log.debug("Getting all versions of process: {}", processKey);
        return processRepository.findAllVersionsByProcessKey(processKey)
                .stream()
                .map(processMapper::toVersionInfoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get BPMN XML for a process
     */
    public String getBpmnXml(Long id) {
        log.debug("Getting BPMN XML for process ID: {}", id);
        ProcessDefinition process = findProcessById(id);
        return process.getBpmnXml();
    }

    /**
     * Get all templates
     */
    public Page<ProcessDefinitionDTO> getAllTemplates(Pageable pageable) {
        log.debug("Getting all templates, page: {}", pageable.getPageNumber());
        return processRepository.findAllTemplates(pageable)
                .map(processMapper::toDTO);
    }

    /**
     * Get processes by category
     */
    public Page<ProcessDefinitionDTO> getProcessesByCategory(Long categoryId, Pageable pageable) {
        log.debug("Getting processes for category ID: {}", categoryId);
        return processRepository.findByCategoryId(categoryId, pageable)
                .map(processMapper::toDTO);
    }

    /**
     * Search processes
     */
    public Page<ProcessDefinitionDTO> searchProcesses(String searchTerm, Pageable pageable) {
        log.debug("Searching processes with term: {}", searchTerm);
        return processRepository.searchProcesses(searchTerm, pageable)
                .map(processMapper::toDTO);
    }

    /**
     * Create new process definition
     * Automatically extracts process key from BPMN XML and sets version to 1
     */
    @Transactional
    public ProcessDefinitionDetailDTO createProcess(CreateProcessDefinitionDTO createDTO) {
        log.debug("Creating new process: {}", createDTO.getName());

        // Validate BPMN XML
        bpmnValidationService.validateBpmnXml(createDTO.getBpmnXml());

        // Extract process key from BPMN
        String processKey = bpmnValidationService.extractProcessKey(createDTO.getBpmnXml());
        log.debug("Extracted process key: {}", processKey);

        // Check if process key already exists
        if (processRepository.existsByProcessKey(processKey)) {
            throw new BusinessException(getMessage("process.key-exists", processKey));
        }

        // Create entity
        ProcessDefinition process = processMapper.toEntity(createDTO);
        process.setProcessKey(processKey);
        process.setVersion(1);
        process.setIsLatestVersion(true);

        // Set category if provided
        if (createDTO.getCategoryId() != null) {
            ProcessCategory category = findCategoryById(createDTO.getCategoryId());
            process.setCategory(category);
        }

        ProcessDefinition saved = processRepository.save(process);

        log.info("Created process: {} v{} (ID: {})", saved.getProcessKey(), saved.getVersion(), saved.getId());
        return processMapper.toDetailDTO(saved);
    }

    /**
     * Update process definition
     * If BPMN XML is changed, creates a new version automatically
     */
    @Transactional
    public ProcessDefinitionDetailDTO updateProcess(Long id, UpdateProcessDefinitionDTO updateDTO) {
        log.debug("Updating process ID: {}", id);

        ProcessDefinition process = findProcessById(id);

        // Check if BPMN XML is being updated
        if (updateDTO.getBpmnXml() != null && !updateDTO.getBpmnXml().equals(process.getBpmnXml())) {
            log.debug("BPMN XML changed, creating new version");
            return createNewVersion(process, updateDTO);
        } else {
            // Update metadata only (no new version)
            return updateMetadata(process, updateDTO);
        }
    }

    /**
     * Create new version of a process
     */
    @Transactional
    public ProcessDefinitionDetailDTO createNewVersion(Long id, String bpmnXml) {
        log.debug("Creating new version for process ID: {}", id);

        ProcessDefinition currentProcess = findProcessById(id);

        // Validate new BPMN XML
        bpmnValidationService.validateBpmnXml(bpmnXml);

        // Verify process key hasn't changed
        String newProcessKey = bpmnValidationService.extractProcessKey(bpmnXml);
        if (!newProcessKey.equals(currentProcess.getProcessKey())) {
            throw new BusinessException(getMessage("process.key-mismatch",
                    currentProcess.getProcessKey(), newProcessKey));
        }

        // Get next version number
        Integer nextVersion = processRepository.findLatestVersionNumber(currentProcess.getProcessKey()) + 1;

        // Mark all previous versions as not latest
        processRepository.markAllVersionsAsNotLatest(currentProcess.getProcessKey());

        // Create new version
        ProcessDefinition newVersion = ProcessDefinition.builder()
                .processKey(currentProcess.getProcessKey())
                .name(currentProcess.getName())
                .version(nextVersion)
                .isLatestVersion(true)
                .bpmnXml(bpmnXml)
                .description(currentProcess.getDescription())
                .category(currentProcess.getCategory())
                .isTemplate(currentProcess.getIsTemplate())
                .published(false) // New version starts as unpublished
                .active(currentProcess.getActive())
                .tags(currentProcess.getTags())
                .build();

        ProcessDefinition saved = processRepository.save(newVersion);

        log.info("Created new version: {} v{} (ID: {})", saved.getProcessKey(), saved.getVersion(), saved.getId());
        return processMapper.toDetailDTO(saved);
    }

    /**
     * Import process from BPMN XML
     */
    @Transactional
    public ProcessDefinitionDetailDTO importProcess(String bpmnXml, Long categoryId, boolean asTemplate) {
        log.debug("Importing process from BPMN XML");

        // Validate BPMN XML
        bpmnValidationService.validateBpmnXml(bpmnXml);

        // Extract metadata
        String processKey = bpmnValidationService.extractProcessKey(bpmnXml);
        String processName = bpmnValidationService.extractProcessName(bpmnXml);

        if (processName == null || processName.trim().isEmpty()) {
            processName = processKey; // Fallback to key if no name
        }

        // Check if process key already exists
        if (processRepository.existsByProcessKey(processKey)) {
            throw new BusinessException(getMessage("process.key-exists", processKey));
        }

        // Create entity
        ProcessDefinition process = ProcessDefinition.builder()
                .processKey(processKey)
                .name(processName)
                .version(1)
                .isLatestVersion(true)
                .bpmnXml(bpmnXml)
                .isTemplate(asTemplate)
                .published(false)
                .active(true)
                .build();

        // Set category if provided
        if (categoryId != null) {
            ProcessCategory category = findCategoryById(categoryId);
            process.setCategory(category);
        }

        ProcessDefinition saved = processRepository.save(process);

        log.info("Imported process: {} v{} (ID: {})", saved.getProcessKey(), saved.getVersion(), saved.getId());
        return processMapper.toDetailDTO(saved);
    }

    /**
     * Delete process (soft delete)
     */
    @Transactional
    public void deleteProcess(Long id) {
        log.debug("Deleting process ID: {}", id);

        ProcessDefinition process = findProcessById(id);

        // TODO: Check if process has active instances

        process.setDeleted(true);
        process.setDeletedAt(LocalDateTime.now());
        processRepository.save(process);

        log.info("Deleted process: {} v{} (ID: {})", process.getProcessKey(), process.getVersion(), process.getId());
    }

    /**
     * Publish process
     */
    @Transactional
    public ProcessDefinitionDTO publishProcess(Long id) {
        log.debug("Publishing process ID: {}", id);

        ProcessDefinition process = findProcessById(id);
        process.setPublished(true);
        ProcessDefinition updated = processRepository.save(process);

        log.info("Published process: {} v{} (ID: {})", updated.getProcessKey(), updated.getVersion(), updated.getId());
        return processMapper.toDTO(updated);
    }

    /**
     * Unpublish process
     */
    @Transactional
    public ProcessDefinitionDTO unpublishProcess(Long id) {
        log.debug("Unpublishing process ID: {}", id);

        ProcessDefinition process = findProcessById(id);
        process.setPublished(false);
        ProcessDefinition updated = processRepository.save(process);

        log.info("Unpublished process: {} v{} (ID: {})", updated.getProcessKey(), updated.getVersion(), updated.getId());
        return processMapper.toDTO(updated);
    }

    /**
     * Update metadata only (no versioning)
     */
    private ProcessDefinitionDetailDTO updateMetadata(ProcessDefinition process, UpdateProcessDefinitionDTO updateDTO) {
        log.debug("Updating metadata for process: {}", process.getProcessKey());

        // Update category if provided
        if (updateDTO.getCategoryId() != null) {
            ProcessCategory category = findCategoryById(updateDTO.getCategoryId());
            process.setCategory(category);
        }

        // Update other fields
        processMapper.updateEntityFromDTO(updateDTO, process);
        ProcessDefinition updated = processRepository.save(process);

        log.info("Updated process metadata: {} v{} (ID: {})",
                 updated.getProcessKey(), updated.getVersion(), updated.getId());
        return processMapper.toDetailDTO(updated);
    }

    /**
     * Create new version from update DTO
     */
    private ProcessDefinitionDetailDTO createNewVersion(ProcessDefinition currentProcess,
                                                        UpdateProcessDefinitionDTO updateDTO) {
        // Validate new BPMN XML
        bpmnValidationService.validateBpmnXml(updateDTO.getBpmnXml());

        // Verify process key hasn't changed
        String newProcessKey = bpmnValidationService.extractProcessKey(updateDTO.getBpmnXml());
        if (!newProcessKey.equals(currentProcess.getProcessKey())) {
            throw new BusinessException(getMessage("process.key-mismatch",
                    currentProcess.getProcessKey(), newProcessKey));
        }

        // Get next version number
        Integer nextVersion = processRepository.findLatestVersionNumber(currentProcess.getProcessKey()) + 1;

        // Mark all previous versions as not latest
        processRepository.markAllVersionsAsNotLatest(currentProcess.getProcessKey());

        // Create new version
        ProcessDefinition newVersion = ProcessDefinition.builder()
                .processKey(currentProcess.getProcessKey())
                .name(updateDTO.getName() != null ? updateDTO.getName() : currentProcess.getName())
                .version(nextVersion)
                .isLatestVersion(true)
                .bpmnXml(updateDTO.getBpmnXml())
                .description(updateDTO.getDescription() != null ? updateDTO.getDescription() : currentProcess.getDescription())
                .category(currentProcess.getCategory())
                .isTemplate(updateDTO.getIsTemplate() != null ? updateDTO.getIsTemplate() : currentProcess.getIsTemplate())
                .published(false) // New version starts as unpublished
                .active(updateDTO.getActive() != null ? updateDTO.getActive() : currentProcess.getActive())
                .tags(updateDTO.getTags() != null ? updateDTO.getTags() : currentProcess.getTags())
                .build();

        // Update category if provided
        if (updateDTO.getCategoryId() != null) {
            ProcessCategory category = findCategoryById(updateDTO.getCategoryId());
            newVersion.setCategory(category);
        }

        ProcessDefinition saved = processRepository.save(newVersion);

        log.info("Created new version from update: {} v{} (ID: {})",
                 saved.getProcessKey(), saved.getVersion(), saved.getId());
        return processMapper.toDetailDTO(saved);
    }

    /**
     * Find process by ID or throw exception
     */
    private ProcessDefinition findProcessById(Long id) {
        return processRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.not-found", id)));
    }

    /**
     * Find category by ID or throw exception
     */
    private ProcessCategory findCategoryById(Long id) {
        return categoryRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.category.not-found", id)));
    }

    /**
     * Get localized message
     */
    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
