package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.instance.ExecutionHistoryDTO;
import com.processmonster.bpm.dto.instance.ProcessInstanceDTO;
import com.processmonster.bpm.dto.instance.StartProcessInstanceDTO;
import com.processmonster.bpm.entity.ExecutionHistory;
import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.ProcessInstance.ProcessInstanceStatus;
import com.processmonster.bpm.mapper.ProcessInstanceMapper;
import com.processmonster.bpm.service.ProcessExecutionService;
import com.processmonster.bpm.service.VariableService;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing process instances
 */
@RestController
@RequestMapping("/api/v1/instances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Process Instances", description = "Endpoints for managing process instance execution")
@SecurityRequirement(name = "bearerAuth")
public class ProcessInstanceController {

    private final ProcessExecutionService executionService;
    private final VariableService variableService;
    private final ProcessInstanceMapper instanceMapper;

    @PostMapping("/start")
    @PreAuthorize("hasAnyAuthority('INSTANCE_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Start process instance", description = "Start a new process instance from a definition")
    @ApiResponse(responseCode = "201", description = "Process instance started successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or process not published/active")
    public ResponseEntity<ProcessInstanceDTO> startProcess(
            @Valid @RequestBody StartProcessInstanceDTO startDTO) {
        log.debug("REST request to start process instance for definition: {}", startDTO.getProcessDefinitionId());

        ProcessInstance instance = executionService.startProcess(
                startDTO.getProcessDefinitionId(),
                startDTO.getBusinessKey(),
                startDTO.getVariables());

        return ResponseEntity.status(HttpStatus.CREATED).body(instanceMapper.toDTO(instance));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all instances", description = "Retrieve paginated list of all process instances")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved instances")
    public ResponseEntity<Page<ProcessInstanceDTO>> getAllInstances(
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all process instances, page: {}", pageable.getPageNumber());

        Page<ProcessInstanceDTO> instances = executionService.getAllInstances(pageable)
                .map(instanceMapper::toDTO);

        return ResponseEntity.ok(instances);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get instance by ID", description = "Retrieve a specific process instance by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved instance")
    @ApiResponse(responseCode = "404", description = "Instance not found")
    public ResponseEntity<ProcessInstanceDTO> getInstanceById(
            @PathVariable @Parameter(description = "Instance ID") Long id) {
        log.debug("REST request to get process instance by ID: {}", id);

        ProcessInstance instance = executionService.getInstanceById(id);
        return ResponseEntity.ok(instanceMapper.toDTO(instance));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get instances by status", description = "Retrieve instances filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved instances")
    public ResponseEntity<Page<ProcessInstanceDTO>> getInstancesByStatus(
            @PathVariable @Parameter(description = "Instance status") ProcessInstanceStatus status,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get instances by status: {}", status);

        Page<ProcessInstanceDTO> instances = executionService.getInstancesByStatus(status, pageable)
                .map(instanceMapper::toDTO);

        return ResponseEntity.ok(instances);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get active instances", description = "Retrieve all active (running or suspended) instances")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active instances")
    public ResponseEntity<Page<ProcessInstanceDTO>> getActiveInstances(
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get active process instances");

        Page<ProcessInstanceDTO> instances = executionService.getActiveInstances(pageable)
                .map(instanceMapper::toDTO);

        return ResponseEntity.ok(instances);
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasAnyAuthority('INSTANCE_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Suspend instance", description = "Suspend a running process instance")
    @ApiResponse(responseCode = "200", description = "Instance suspended successfully")
    @ApiResponse(responseCode = "400", description = "Instance not running")
    public ResponseEntity<ProcessInstanceDTO> suspendInstance(
            @PathVariable @Parameter(description = "Instance ID") Long id,
            @RequestParam(required = false) @Parameter(description = "Suspension reason") String reason) {
        log.debug("REST request to suspend process instance: {}", id);

        ProcessInstance instance = executionService.suspendProcess(id, reason);
        return ResponseEntity.ok(instanceMapper.toDTO(instance));
    }

    @PutMapping("/{id}/resume")
    @PreAuthorize("hasAnyAuthority('INSTANCE_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Resume instance", description = "Resume a suspended process instance")
    @ApiResponse(responseCode = "200", description = "Instance resumed successfully")
    @ApiResponse(responseCode = "400", description = "Instance not suspended")
    public ResponseEntity<ProcessInstanceDTO> resumeInstance(
            @PathVariable @Parameter(description = "Instance ID") Long id) {
        log.debug("REST request to resume process instance: {}", id);

        ProcessInstance instance = executionService.resumeProcess(id);
        return ResponseEntity.ok(instanceMapper.toDTO(instance));
    }

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasAnyAuthority('INSTANCE_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Terminate instance", description = "Terminate a process instance")
    @ApiResponse(responseCode = "200", description = "Instance terminated successfully")
    public ResponseEntity<ProcessInstanceDTO> terminateInstance(
            @PathVariable @Parameter(description = "Instance ID") Long id,
            @RequestParam(required = false) @Parameter(description = "Termination reason") String reason) {
        log.debug("REST request to terminate process instance: {}", id);

        ProcessInstance instance = executionService.terminateProcess(id, reason);
        return ResponseEntity.ok(instanceMapper.toDTO(instance));
    }

    @GetMapping("/{id}/variables")
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get variables", description = "Get all variables for a process instance")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved variables")
    public ResponseEntity<Map<String, Object>> getVariables(
            @PathVariable @Parameter(description = "Instance ID") Long id) {
        log.debug("REST request to get variables for instance: {}", id);

        Map<String, Object> variables = variableService.getVariables(id);
        return ResponseEntity.ok(variables);
    }

    @PutMapping("/{id}/variables")
    @PreAuthorize("hasAnyAuthority('INSTANCE_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Set variables", description = "Set or update variables for a process instance")
    @ApiResponse(responseCode = "200", description = "Variables updated successfully")
    public ResponseEntity<Void> setVariables(
            @PathVariable @Parameter(description = "Instance ID") Long id,
            @RequestBody Map<String, Object> variables) {
        log.debug("REST request to set variables for instance: {}", id);

        variableService.setVariables(id, variables);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('INSTANCE_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get execution history", description = "Get complete execution history for an instance")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved history")
    public ResponseEntity<List<ExecutionHistoryDTO>> getInstanceHistory(
            @PathVariable @Parameter(description = "Instance ID") Long id) {
        log.debug("REST request to get history for instance: {}", id);

        List<ExecutionHistory> history = executionService.getInstanceHistory(id);
        List<ExecutionHistoryDTO> historyDTOs = history.stream()
                .map(instanceMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historyDTOs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INSTANCE_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete instance", description = "Delete a process instance (soft delete)")
    @ApiResponse(responseCode = "204", description = "Instance deleted successfully")
    @ApiResponse(responseCode = "400", description = "Instance is still active")
    public ResponseEntity<Void> deleteInstance(
            @PathVariable @Parameter(description = "Instance ID") Long id) {
        log.debug("REST request to delete process instance: {}", id);

        executionService.deleteInstance(id);
        return ResponseEntity.noContent().build();
    }
}
