package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.process.CreateProcessCategoryDTO;
import com.processmonster.bpm.dto.process.ProcessCategoryDTO;
import com.processmonster.bpm.dto.process.UpdateProcessCategoryDTO;
import com.processmonster.bpm.service.ProcessCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing process categories
 */
@RestController
@RequestMapping("/api/v1/process-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Process Categories", description = "Endpoints for managing process categories")
@SecurityRequirement(name = "bearerAuth")
public class ProcessCategoryController {

    private final ProcessCategoryService processCategoryService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all process categories", description = "Retrieve paginated list of all process categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    public ResponseEntity<Page<ProcessCategoryDTO>> getAllCategories(
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all process categories, page: {}", pageable.getPageNumber());
        Page<ProcessCategoryDTO> categories = processCategoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all active categories", description = "Retrieve all active process categories (for dropdowns)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories")
    public ResponseEntity<List<ProcessCategoryDTO>> getAllActiveCategories() {
        log.debug("REST request to get all active process categories");
        List<ProcessCategoryDTO> categories = processCategoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific process category by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ProcessCategoryDTO> getCategoryById(
            @PathVariable @Parameter(description = "Category ID") Long id) {
        log.debug("REST request to get process category by ID: {}", id);
        ProcessCategoryDTO category = processCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get category by code", description = "Retrieve a specific process category by its code")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ProcessCategoryDTO> getCategoryByCode(
            @PathVariable @Parameter(description = "Category code") String code) {
        log.debug("REST request to get process category by code: {}", code);
        ProcessCategoryDTO category = processCategoryService.getCategoryByCode(code);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search categories", description = "Search process categories by name or code")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching categories")
    public ResponseEntity<Page<ProcessCategoryDTO>> searchCategories(
            @RequestParam @Parameter(description = "Search term") String keyword,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to search process categories with keyword: {}", keyword);
        Page<ProcessCategoryDTO> categories = processCategoryService.searchCategories(keyword, pageable);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROCESS_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create category", description = "Create a new process category")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or code already exists")
    public ResponseEntity<ProcessCategoryDTO> createCategory(
            @Valid @RequestBody CreateProcessCategoryDTO createDTO) {
        log.debug("REST request to create process category: {}", createDTO.getCode());
        ProcessCategoryDTO created = processCategoryService.createCategory(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing process category")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or code already exists")
    public ResponseEntity<ProcessCategoryDTO> updateCategory(
            @PathVariable @Parameter(description = "Category ID") Long id,
            @Valid @RequestBody UpdateProcessCategoryDTO updateDTO) {
        log.debug("REST request to update process category ID: {}", id);
        ProcessCategoryDTO updated = processCategoryService.updateCategory(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a process category (soft delete)")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "400", description = "Category has active processes")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Parameter(description = "Category ID") Long id) {
        log.debug("REST request to delete process category ID: {}", id);
        processCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Activate category", description = "Activate a process category")
    @ApiResponse(responseCode = "200", description = "Category activated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ProcessCategoryDTO> activateCategory(
            @PathVariable @Parameter(description = "Category ID") Long id) {
        log.debug("REST request to activate process category ID: {}", id);
        ProcessCategoryDTO activated = processCategoryService.activateCategory(id);
        return ResponseEntity.ok(activated);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Deactivate category", description = "Deactivate a process category")
    @ApiResponse(responseCode = "200", description = "Category deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ProcessCategoryDTO> deactivateCategory(
            @PathVariable @Parameter(description = "Category ID") Long id) {
        log.debug("REST request to deactivate process category ID: {}", id);
        ProcessCategoryDTO deactivated = processCategoryService.deactivateCategory(id);
        return ResponseEntity.ok(deactivated);
    }
}
