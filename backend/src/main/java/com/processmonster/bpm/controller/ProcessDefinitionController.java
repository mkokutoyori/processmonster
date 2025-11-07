package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.process.CreateProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDetailDTO;
import com.processmonster.bpm.dto.process.ProcessVersionInfoDTO;
import com.processmonster.bpm.dto.process.UpdateProcessDefinitionDTO;
import com.processmonster.bpm.service.ProcessDefinitionService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing process definitions
 */
@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Process Definitions", description = "Endpoints for managing BPMN process definitions and versioning")
@SecurityRequirement(name = "bearerAuth")
public class ProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all processes", description = "Retrieve paginated list of all process definitions (latest versions only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved processes")
    public ResponseEntity<Page<ProcessDefinitionDTO>> getAllProcesses(
            @RequestParam(required = false, defaultValue = "false")
            @Parameter(description = "Include all versions (not just latest)") boolean allVersions,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all processes, allVersions: {}, page: {}", allVersions, pageable.getPageNumber());
        Page<ProcessDefinitionDTO> processes = allVersions ?
                processDefinitionService.getAllProcessesAllVersions(pageable) :
                processDefinitionService.getAllProcesses(pageable);
        return ResponseEntity.ok(processes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get process by ID", description = "Retrieve a specific process definition by its ID (includes BPMN XML)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved process")
    @ApiResponse(responseCode = "404", description = "Process not found")
    public ResponseEntity<ProcessDefinitionDetailDTO> getProcessById(
            @PathVariable @Parameter(description = "Process ID") Long id) {
        log.debug("REST request to get process by ID: {}", id);
        ProcessDefinitionDetailDTO process = processDefinitionService.getProcessById(id);
        return ResponseEntity.ok(process);
    }

    @GetMapping("/key/{processKey}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get latest version by key", description = "Retrieve the latest version of a process by its key")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved process")
    @ApiResponse(responseCode = "404", description = "Process not found")
    public ResponseEntity<ProcessDefinitionDetailDTO> getLatestVersionByKey(
            @PathVariable @Parameter(description = "Process key") String processKey) {
        log.debug("REST request to get latest version of process: {}", processKey);
        ProcessDefinitionDetailDTO process = processDefinitionService.getLatestVersionByKey(processKey);
        return ResponseEntity.ok(process);
    }

    @GetMapping("/key/{processKey}/version/{version}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get specific version", description = "Retrieve a specific version of a process")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved process")
    @ApiResponse(responseCode = "404", description = "Process version not found")
    public ResponseEntity<ProcessDefinitionDetailDTO> getProcessByKeyAndVersion(
            @PathVariable @Parameter(description = "Process key") String processKey,
            @PathVariable @Parameter(description = "Version number") Integer version) {
        log.debug("REST request to get process {}:v{}", processKey, version);
        ProcessDefinitionDetailDTO process = processDefinitionService.getProcessByKeyAndVersion(processKey, version);
        return ResponseEntity.ok(process);
    }

    @GetMapping("/key/{processKey}/versions")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all versions", description = "Retrieve all versions of a process by its key")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved versions")
    public ResponseEntity<List<ProcessVersionInfoDTO>> getAllVersionsByKey(
            @PathVariable @Parameter(description = "Process key") String processKey) {
        log.debug("REST request to get all versions of process: {}", processKey);
        List<ProcessVersionInfoDTO> versions = processDefinitionService.getAllVersionsByKey(processKey);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/xml")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get BPMN XML", description = "Retrieve the BPMN XML content of a process")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved BPMN XML")
    @ApiResponse(responseCode = "404", description = "Process not found")
    public ResponseEntity<String> getBpmnXml(
            @PathVariable @Parameter(description = "Process ID") Long id) {
        log.debug("REST request to get BPMN XML for process ID: {}", id);
        String bpmnXml = processDefinitionService.getBpmnXml(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(bpmnXml);
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all templates", description = "Retrieve all process templates")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved templates")
    public ResponseEntity<Page<ProcessDefinitionDTO>> getAllTemplates(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all process templates, page: {}", pageable.getPageNumber());
        Page<ProcessDefinitionDTO> templates = processDefinitionService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get processes by category", description = "Retrieve all processes in a specific category")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved processes")
    public ResponseEntity<Page<ProcessDefinitionDTO>> getProcessesByCategory(
            @PathVariable @Parameter(description = "Category ID") Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get processes by category ID: {}", categoryId);
        Page<ProcessDefinitionDTO> processes = processDefinitionService.getProcessesByCategory(categoryId, pageable);
        return ResponseEntity.ok(processes);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('PROCESS_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search processes", description = "Search processes by name, key or tags")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching processes")
    public ResponseEntity<Page<ProcessDefinitionDTO>> searchProcesses(
            @RequestParam @Parameter(description = "Search term") String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to search processes with keyword: {}", keyword);
        Page<ProcessDefinitionDTO> processes = processDefinitionService.searchProcesses(keyword, pageable);
        return ResponseEntity.ok(processes);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROCESS_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create process", description = "Create a new process definition from BPMN XML")
    @ApiResponse(responseCode = "201", description = "Process created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid BPMN XML or process key already exists")
    public ResponseEntity<ProcessDefinitionDetailDTO> createProcess(
            @Valid @RequestBody CreateProcessDefinitionDTO createDTO) {
        log.debug("REST request to create process: {}", createDTO.getName());
        ProcessDefinitionDetailDTO created = processDefinitionService.createProcess(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update process", description = "Update a process definition. If BPMN XML is changed, creates a new version automatically")
    @ApiResponse(responseCode = "200", description = "Process updated successfully (or new version created)")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or BPMN XML")
    public ResponseEntity<ProcessDefinitionDetailDTO> updateProcess(
            @PathVariable @Parameter(description = "Process ID") Long id,
            @Valid @RequestBody UpdateProcessDefinitionDTO updateDTO) {
        log.debug("REST request to update process ID: {}", id);
        ProcessDefinitionDetailDTO updated = processDefinitionService.updateProcess(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/new-version")
    @PreAuthorize("hasAnyAuthority('PROCESS_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create new version", description = "Create a new version of a process from updated BPMN XML")
    @ApiResponse(responseCode = "201", description = "New version created successfully")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "400", description = "Invalid BPMN XML or process key mismatch")
    public ResponseEntity<ProcessDefinitionDetailDTO> createNewVersion(
            @PathVariable @Parameter(description = "Process ID") Long id,
            @RequestBody @Parameter(description = "New BPMN XML content") String bpmnXml) {
        log.debug("REST request to create new version for process ID: {}", id);
        ProcessDefinitionDetailDTO newVersion = processDefinitionService.createNewVersion(id, bpmnXml);
        return ResponseEntity.status(HttpStatus.CREATED).body(newVersion);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('PROCESS_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Import BPMN", description = "Import a process from BPMN XML file")
    @ApiResponse(responseCode = "201", description = "Process imported successfully")
    @ApiResponse(responseCode = "400", description = "Invalid BPMN XML or process key already exists")
    public ResponseEntity<ProcessDefinitionDetailDTO> importProcess(
            @RequestBody @Parameter(description = "BPMN XML content") String bpmnXml,
            @RequestParam(required = false) @Parameter(description = "Category ID") Long categoryId,
            @RequestParam(required = false, defaultValue = "false")
            @Parameter(description = "Import as template") boolean asTemplate) {
        log.debug("REST request to import process from BPMN XML, asTemplate: {}", asTemplate);
        ProcessDefinitionDetailDTO imported = processDefinitionService.importProcess(bpmnXml, categoryId, asTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROCESS_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete process", description = "Delete a process definition (soft delete)")
    @ApiResponse(responseCode = "204", description = "Process deleted successfully")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "400", description = "Process has active instances")
    public ResponseEntity<Void> deleteProcess(
            @PathVariable @Parameter(description = "Process ID") Long id) {
        log.debug("REST request to delete process ID: {}", id);
        processDefinitionService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Publish process", description = "Publish a process definition")
    @ApiResponse(responseCode = "200", description = "Process published successfully")
    @ApiResponse(responseCode = "404", description = "Process not found")
    public ResponseEntity<ProcessDefinitionDTO> publishProcess(
            @PathVariable @Parameter(description = "Process ID") Long id) {
        log.debug("REST request to publish process ID: {}", id);
        ProcessDefinitionDTO published = processDefinitionService.publishProcess(id);
        return ResponseEntity.ok(published);
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyAuthority('PROCESS_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Unpublish process", description = "Unpublish a process definition")
    @ApiResponse(responseCode = "200", description = "Process unpublished successfully")
    @ApiResponse(responseCode = "404", description = "Process not found")
    public ResponseEntity<ProcessDefinitionDTO> unpublishProcess(
            @PathVariable @Parameter(description = "Process ID") Long id) {
        log.debug("REST request to unpublish process ID: {}", id);
        ProcessDefinitionDTO unpublished = processDefinitionService.unpublishProcess(id);
        return ResponseEntity.ok(unpublished);
    }
}
