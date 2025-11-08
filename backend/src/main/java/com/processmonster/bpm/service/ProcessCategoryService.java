package com.processmonster.bpm.service;

import com.processmonster.bpm.dto.process.CreateProcessCategoryDTO;
import com.processmonster.bpm.dto.process.ProcessCategoryDTO;
import com.processmonster.bpm.dto.process.UpdateProcessCategoryDTO;
import com.processmonster.bpm.entity.ProcessCategory;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.mapper.ProcessCategoryMapper;
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
 * Service for managing process categories
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessCategoryService {

    private final ProcessCategoryRepository categoryRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ProcessCategoryMapper categoryMapper;
    private final MessageSource messageSource;

    /**
     * Get all categories (paginated)
     */
    public Page<ProcessCategoryDTO> getAllCategories(Pageable pageable) {
        log.debug("Getting all categories, page: {}", pageable.getPageNumber());
        return categoryRepository.findByDeletedFalse(pageable)
                .map(this::mapToDTOWithProcessCount);
    }

    /**
     * Get all active categories (for dropdowns)
     */
    public List<ProcessCategoryDTO> getAllActiveCategories() {
        log.debug("Getting all active categories");
        return categoryRepository.findAllActiveOrderedByDisplayOrder()
                .stream()
                .map(this::mapToDTOWithProcessCount)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    public ProcessCategoryDTO getCategoryById(Long id) {
        log.debug("Getting category by ID: {}", id);
        ProcessCategory category = findCategoryById(id);
        return mapToDTOWithProcessCount(category);
    }

    /**
     * Get category by code
     */
    public ProcessCategoryDTO getCategoryByCode(String code) {
        log.debug("Getting category by code: {}", code);
        ProcessCategory category = categoryRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("process.category.not-found-code", code)));
        return mapToDTOWithProcessCount(category);
    }

    /**
     * Search categories
     */
    public Page<ProcessCategoryDTO> searchCategories(String searchTerm, Pageable pageable) {
        log.debug("Searching categories with term: {}", searchTerm);
        return categoryRepository.searchCategories(searchTerm, pageable)
                .map(this::mapToDTOWithProcessCount);
    }

    /**
     * Create new category
     */
    @Transactional
    public ProcessCategoryDTO createCategory(CreateProcessCategoryDTO createDTO) {
        log.debug("Creating new category with code: {}", createDTO.getCode());

        // Check if code already exists
        if (categoryRepository.existsByCodeIgnoreCase(createDTO.getCode())) {
            throw new BusinessException(getMessage("process.category.code-exists", createDTO.getCode()));
        }

        ProcessCategory category = categoryMapper.toEntity(createDTO);
        ProcessCategory saved = categoryRepository.save(category);

        log.info("Created category: {} (ID: {})", saved.getCode(), saved.getId());
        return categoryMapper.toDTO(saved);
    }

    /**
     * Update category
     */
    @Transactional
    public ProcessCategoryDTO updateCategory(Long id, UpdateProcessCategoryDTO updateDTO) {
        log.debug("Updating category ID: {}", id);

        ProcessCategory category = findCategoryById(id);

        // Check if new code conflicts with existing category
        if (updateDTO.getCode() != null &&
            !updateDTO.getCode().equalsIgnoreCase(category.getCode()) &&
            categoryRepository.existsByCodeIgnoreCaseAndIdNot(updateDTO.getCode(), id)) {
            throw new BusinessException(getMessage("process.category.code-exists", updateDTO.getCode()));
        }

        categoryMapper.updateEntityFromDTO(updateDTO, category);
        ProcessCategory updated = categoryRepository.save(category);

        log.info("Updated category: {} (ID: {})", updated.getCode(), updated.getId());
        return categoryMapper.toDTO(updated);
    }

    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category ID: {}", id);

        ProcessCategory category = findCategoryById(id);

        // Check if category has processes
        long processCount = processDefinitionRepository.countByCategoryId(id);
        if (processCount > 0) {
            throw new BusinessException(getMessage("process.category.has-processes", processCount));
        }

        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        log.info("Deleted category: {} (ID: {})", category.getCode(), category.getId());
    }

    /**
     * Activate category
     */
    @Transactional
    public ProcessCategoryDTO activateCategory(Long id) {
        log.debug("Activating category ID: {}", id);

        ProcessCategory category = findCategoryById(id);
        category.setActive(true);
        ProcessCategory updated = categoryRepository.save(category);

        log.info("Activated category: {} (ID: {})", updated.getCode(), updated.getId());
        return categoryMapper.toDTO(updated);
    }

    /**
     * Deactivate category
     */
    @Transactional
    public ProcessCategoryDTO deactivateCategory(Long id) {
        log.debug("Deactivating category ID: {}", id);

        ProcessCategory category = findCategoryById(id);
        category.setActive(false);
        ProcessCategory updated = categoryRepository.save(category);

        log.info("Deactivated category: {} (ID: {})", updated.getCode(), updated.getId());
        return categoryMapper.toDTO(updated);
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
     * Map to DTO with process count
     */
    private ProcessCategoryDTO mapToDTOWithProcessCount(ProcessCategory category) {
        ProcessCategoryDTO dto = categoryMapper.toDTO(category);
        long processCount = processDefinitionRepository.countByCategoryId(category.getId());
        dto.setProcessCount(processCount);
        return dto;
    }

    /**
     * Get localized message
     */
    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
