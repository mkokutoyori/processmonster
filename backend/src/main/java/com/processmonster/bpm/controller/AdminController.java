package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.admin.CreateSystemParameterDTO;
import com.processmonster.bpm.dto.admin.SystemParameterDTO;
import com.processmonster.bpm.dto.admin.UpdateSystemParameterDTO;
import com.processmonster.bpm.entity.SystemParameter;
import com.processmonster.bpm.mapper.SystemParameterMapper;
import com.processmonster.bpm.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for system administration.
 * Manages system parameters and configuration.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "System administration and configuration")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final SystemService systemService;
    private final SystemParameterMapper systemParameterMapper;

    @PostMapping("/parameters")
    @PreAuthorize("hasAnyAuthority('ADMIN_WRITE', 'ROLE_ADMIN')")
    @Operation(summary = "Create system parameter", description = "Create a new system parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parameter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<SystemParameterDTO> createParameter(@Valid @RequestBody CreateSystemParameterDTO createDTO) {
        log.debug("REST request to create system parameter: {}", createDTO.getKey());

        SystemParameter param = systemService.createParameter(
                createDTO.getKey(),
                createDTO.getValue(),
                createDTO.getDescription(),
                createDTO.getCategory(),
                createDTO.getDataType(),
                createDTO.getDefaultValue(),
                createDTO.getEncrypted(),
                createDTO.getEditable(),
                createDTO.getValidationPattern(),
                createDTO.getAllowedValues(),
                createDTO.getDisplayOrder()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(systemParameterMapper.toDTO(param));
    }

    @GetMapping("/parameters/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get parameter by ID", description = "Retrieve system parameter by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parameter not found")
    })
    public ResponseEntity<SystemParameterDTO> getParameterById(@PathVariable Long id) {
        log.debug("REST request to get system parameter: {}", id);

        SystemParameter param = systemService.getParameterById(id);
        return ResponseEntity.ok(systemParameterMapper.toDTO(param));
    }

    @GetMapping("/parameters")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get all parameters", description = "Retrieve all system parameters (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameters retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<SystemParameterDTO>> getAllParameters(Pageable pageable) {
        log.debug("REST request to get all system parameters");

        Page<SystemParameter> params = systemService.getAllParameters(pageable);
        Page<SystemParameterDTO> dtos = params.map(systemParameterMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/parameters/category/{category}")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get parameters by category", description = "Retrieve system parameters by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameters retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<SystemParameterDTO>> getParametersByCategory(@PathVariable String category) {
        log.debug("REST request to get system parameters by category: {}", category);

        List<SystemParameter> params = systemService.getParametersByCategory(category);
        List<SystemParameterDTO> dtos = params.stream()
                .map(systemParameterMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/parameters/editable")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get editable parameters", description = "Retrieve all editable system parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Editable parameters retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<SystemParameterDTO>> getEditableParameters() {
        log.debug("REST request to get editable system parameters");

        List<SystemParameter> params = systemService.getEditableParameters();
        List<SystemParameterDTO> dtos = params.stream()
                .map(systemParameterMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/parameters/categories")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get all categories", description = "Retrieve all parameter categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<String>> getAllCategories() {
        log.debug("REST request to get all parameter categories");

        List<String> categories = systemService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/parameters/search")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Search parameters", description = "Search system parameters by keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<SystemParameterDTO>> searchParameters(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            Pageable pageable) {
        log.debug("REST request to search system parameters: {}", keyword);

        Page<SystemParameter> params = systemService.searchParameters(keyword, pageable);
        Page<SystemParameterDTO> dtos = params.map(systemParameterMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/parameters/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_WRITE', 'ROLE_ADMIN')")
    @Operation(summary = "Update parameter", description = "Update system parameter configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parameter not found")
    })
    public ResponseEntity<SystemParameterDTO> updateParameter(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSystemParameterDTO updateDTO) {
        log.debug("REST request to update system parameter: {}", id);

        SystemParameter updated = systemService.updateParameter(
                id,
                updateDTO.getDescription(),
                updateDTO.getCategory(),
                updateDTO.getValidationPattern(),
                updateDTO.getAllowedValues(),
                updateDTO.getDisplayOrder(),
                updateDTO.getEditable()
        );

        return ResponseEntity.ok(systemParameterMapper.toDTO(updated));
    }

    @PutMapping("/parameters/{id}/value")
    @PreAuthorize("hasAnyAuthority('ADMIN_WRITE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Update parameter value", description = "Update system parameter value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter value updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parameter not found")
    })
    public ResponseEntity<SystemParameterDTO> updateParameterValue(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.debug("REST request to update system parameter value: {}", id);

        String newValue = body.get("value");
        SystemParameter updated = systemService.updateParameterValue(id, newValue);

        return ResponseEntity.ok(systemParameterMapper.toDTO(updated));
    }

    @PutMapping("/parameters/{id}/reset")
    @PreAuthorize("hasAnyAuthority('ADMIN_WRITE', 'ROLE_ADMIN')")
    @Operation(summary = "Reset parameter to default", description = "Reset system parameter to its default value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter reset successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parameter not found")
    })
    public ResponseEntity<SystemParameterDTO> resetToDefault(@PathVariable Long id) {
        log.debug("REST request to reset system parameter to default: {}", id);

        SystemParameter reset = systemService.resetToDefault(id);
        return ResponseEntity.ok(systemParameterMapper.toDTO(reset));
    }

    @DeleteMapping("/parameters/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_WRITE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete parameter", description = "Delete system parameter (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parameter deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parameter not found")
    })
    public ResponseEntity<Void> deleteParameter(@PathVariable Long id) {
        log.debug("REST request to delete system parameter: {}", id);

        systemService.deleteParameter(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get system configuration", description = "Get all system parameters as a configuration map")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, String>> getSystemConfiguration() {
        log.debug("REST request to get system configuration");

        Map<String, String> config = systemService.getSystemConfiguration();
        return ResponseEntity.ok(config);
    }

    @GetMapping("/config/{category}")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get configuration by category", description = "Get system parameters by category as a configuration map")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, String>> getSystemConfigurationByCategory(@PathVariable String category) {
        log.debug("REST request to get system configuration by category: {}", category);

        Map<String, String> config = systemService.getSystemConfigurationByCategory(category);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get admin statistics", description = "Get statistics about system parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        log.debug("REST request to get admin statistics");

        List<SystemParameter> allParams = systemService.getAllParameters();
        List<String> categories = systemService.getAllCategories();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalParameters", allParams.size());
        stats.put("totalCategories", categories.size());
        stats.put("editableParameters", allParams.stream().filter(SystemParameter::getEditable).count());
        stats.put("encryptedParameters", allParams.stream().filter(SystemParameter::getEncrypted).count());

        return ResponseEntity.ok(stats);
    }
}
